package com.messi.system.consistency.scheduler;

import com.messi.system.consistency.manager.TaskExecManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

/**
 * 最终一致性任务调度器
 */
@Slf4j
@Component
public class ConsistencyTaskScheduler {

    @Autowired
    private TaskExecManager taskExecManager;

    /**
     * 在外部业务系统的定时任务中调用
     * 执行记录在本地信息表中，未执行成功的最终一致性任务
     */
    public void execTask() {
        try {
            log.info("ConsistencyTaskScheduler 开始执行任务");
            taskExecManager.execTask();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
