package com.messi.system.consistency.service;

import com.messi.system.consistency.instance.ConsistencyTaskInstance;

import java.util.List;

/**
 * 任务实例service
 */
public interface TaskInstanceService {

    /**
     * 保存任务实例
     *
     * @param consistencyTaskInstance 最终一致性任务实例
     */
    void saveTaskInstance(ConsistencyTaskInstance consistencyTaskInstance);

    /**
     * 获取所有未完成最终一致性任务的列表
     *
     * @return 未完成的最终一致性任务列表
     */
    List<ConsistencyTaskInstance> getUnfinishedTaskList();

    /**
     * 给最终一致性任务配置启动项
     *
     * @param consistencyTaskInstance 最终一致性任务实例
     */
    ConsistencyTaskInstance configureStartupItems(ConsistencyTaskInstance consistencyTaskInstance);

    /**
     * 更新任务实例
     *
     * @param consistencyTaskInstance 最终一致性任务实例
     */
    void updateTaskInstance(ConsistencyTaskInstance consistencyTaskInstance);

    /**
     * 删除任务实例
     *
     * @param consistencyTaskInstance 最终一致性任务实例
     */
    void removeTaskInstance(ConsistencyTaskInstance consistencyTaskInstance);

    /**
     * 更新标记执行失败的任务实例
     *
     * @param consistencyTaskInstance 最终一致性任务实例
     */
    void updateFailedTaskInstance(ConsistencyTaskInstance consistencyTaskInstance);
}
