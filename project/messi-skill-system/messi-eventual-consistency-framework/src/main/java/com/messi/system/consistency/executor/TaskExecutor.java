package com.messi.system.consistency.executor;

import com.messi.system.consistency.instance.ConsistencyTaskInstance;

/**
 * 任务执行器
 */
public interface TaskExecutor {

    /**
     * 执行最终一致性任务实例
     *
     * @param consistencyTaskInstance 任务实例
     */
    ConsistencyTaskInstance execTaskInstance(ConsistencyTaskInstance consistencyTaskInstance);
}
