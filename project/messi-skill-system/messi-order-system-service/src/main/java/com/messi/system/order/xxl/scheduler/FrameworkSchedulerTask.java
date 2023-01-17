package com.messi.system.order.xxl.scheduler;

import com.messi.system.consistency.scheduler.ConsistencyTaskScheduler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 最终一致性任务定时执行器
 */
@Slf4j
@Component
public class FrameworkSchedulerTask {

    /**
     * 任务调度器
     */
    @Autowired
    private ConsistencyTaskScheduler taskScheduler;

    /**
     * 执行最终一致性任务
     */
    @XxlJob("frameworkSchedulerTask")
    public void execConsistencyTask() {
        try {
            taskScheduler.execTask();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}