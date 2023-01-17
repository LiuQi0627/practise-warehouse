package com.messi.system.consistency.service.impl;

import com.messi.system.consistency.enums.TaskExecStatusEnums;
import com.messi.system.consistency.mapper.TaskInstanceMapper;
import com.messi.system.consistency.instance.ConsistencyTaskInstance;
import com.messi.system.consistency.query.TaskQueryRange;
import com.messi.system.consistency.service.TaskInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 任务实例service实现类
 */
@Slf4j
@Service
public class TaskInstanceServiceImpl implements TaskInstanceService {

    @Autowired
    private TaskInstanceMapper taskInstanceMapper;

    @Autowired
    private TaskQueryRange taskQueryRange;

    @Override
    public void saveTaskInstance(ConsistencyTaskInstance consistencyTaskInstance) {
        taskInstanceMapper.saveTask(consistencyTaskInstance);
    }

    @Override
    public List<ConsistencyTaskInstance> getUnfinishedTaskList() {
        Date startTime = taskQueryRange.getStartTime();
        Date endTime = taskQueryRange.getEndTime();
        Long limitTask = taskQueryRange.getLimitTask();

        return taskInstanceMapper.getUnfinishedTaskList(startTime, endTime, limitTask);
    }

    @Override
    public ConsistencyTaskInstance configureStartupItems(ConsistencyTaskInstance consistencyTaskInstance) {
        consistencyTaskInstance.setExecTime(new Date());
        consistencyTaskInstance.setStatus(TaskExecStatusEnums.RUNNING.getCode());
        consistencyTaskInstance.setExecTotal(consistencyTaskInstance.getExecTotal() + 1);
        return consistencyTaskInstance;
    }

    @Override
    public void updateTaskInstance(ConsistencyTaskInstance consistencyTaskInstance) {
        taskInstanceMapper.update(consistencyTaskInstance);
    }

    @Override
    public void removeTaskInstance(ConsistencyTaskInstance consistencyTaskInstance) {
        taskInstanceMapper.remove(consistencyTaskInstance);
    }

    @Override
    public void updateFailedTaskInstance(ConsistencyTaskInstance consistencyTaskInstance) {
        //  重新计算任务的执行时间 = 执行次数+1 * 任务的执行间隔
        int newExecTotal = consistencyTaskInstance.getExecTotal() + 1;
        long newCurrentTime = System.currentTimeMillis() + (long) newExecTotal * consistencyTaskInstance.getExecInterval();
        consistencyTaskInstance.setExecTime(new Date(newCurrentTime));
        consistencyTaskInstance.setModifiedTime(new Date());

        taskInstanceMapper.updateFailed(consistencyTaskInstance);
    }

}
