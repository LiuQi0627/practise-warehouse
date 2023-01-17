package com.messi.system.consistency.manager;

import com.messi.system.consistency.executor.TaskExecutor;
import com.messi.system.consistency.instance.ConsistencyTaskInstance;
import com.messi.system.consistency.service.TaskInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.*;

/**
 * 任务执行管理器
 */
@Slf4j
@Component
public class TaskExecManager {

    /**
     * 任务服务
     */
    @Autowired
    private TaskInstanceService taskInstanceService;

    /**
     * 任务执行器
     */
    @Autowired
    private TaskExecutor taskExecutor;

    //  固定线程池
    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            5,
            5,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(500)
    );

    /**
     * 配合倒数闩使用执行
     */
    private final CompletionService<ConsistencyTaskInstance> completionService = new ExecutorCompletionService<>(threadPoolExecutor);

    /**
     * 查询并且执行未完成的一致性任务
     * 通过业务微服务的xxl-job定时任务调用
     */
    public void execTask() throws InterruptedException, ExecutionException {
        //  1、查询记录在本地消息表中的最终用一致性任务
        List<ConsistencyTaskInstance> unfinishedTaskList = taskInstanceService.getUnfinishedTaskList();

        //  2、如果全部执行，本次任务结束
        if (CollectionUtils.isEmpty(unfinishedTaskList)) {
            log.info("本地消息表中没有待执行的一致性任务,流程结束。");
            return;
        }

        //  3、执行任务
        //  使用倒数闩并发执行
        CountDownLatch latch = new CountDownLatch(unfinishedTaskList.size());
        for (ConsistencyTaskInstance instance : unfinishedTaskList) {
            //  没有使用线程池来做，而是使用CompletionService来做的原因：
            //  如果最终一致性任务实例非常的多，并且 线程池设置的过小，线程池会拒绝任务
            Future<ConsistencyTaskInstance> future = this.completionService.submit(() -> {
                try {
                    return taskExecutor.execTaskInstance(instance);
                } finally {
                    latch.countDown();
                }
            });

            log.info("最终一致性任务实例：{}，已处理", future.get().getTask());
        }

        latch.await();  //  等待线程实例全部执行完
        log.info("本地消息表中的最终一致性框架任务实例本次已全部处理。");
    }

}
