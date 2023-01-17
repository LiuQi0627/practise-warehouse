package com.messi.system.consistency.util;

import com.messi.system.consistency.enums.TaskExecTypeEnums;

import java.util.function.Supplier;

/**
 * 记录本地线程任务执行标记
 */
public class ThreadLocalFlagUtil {

    /**
     * 本地线程的任务执行标记，0是未执行，1是已执行
     */
    private static final ThreadLocal<Integer> execFlag = ThreadLocal.withInitial(new Supplier<Integer>() {
        @Override
        public Integer get() {
            return TaskExecTypeEnums.TASK_NOT_EXECUTED.getCode();
        }
    });

    /**
     * 标记已执行
     */
    public static void markExecuted() {
        execFlag.set(TaskExecTypeEnums.TASK_EXECUTING.getCode());
    }

    /**
     * 标记未执行
     */
    public static void markNotExecuted() {
        execFlag.set(TaskExecTypeEnums.TASK_EXECUTING.getCode());
    }

    /**
     * 获取执行标记
     */
    public static Integer getExecFlag() {
        return execFlag.get();
    }

}
