package com.messi.system.consistency.enums;

/**
 * 最终一致性任务的执行类型常量枚举
 */
public enum TaskExecTypeEnums {
    /**
     * 立即执行
     */
    IMMEDIATELY_EXECUTE(0, "立即执行"),

    /**
     * 延迟执行
     */
    DELAYED_EXECUTION(1, "延迟执行"),

    /**
     * 任务未执行
     */
    TASK_NOT_EXECUTED(0, "此任务未执行"),

    /**
     * 任务执行中
     */
    TASK_EXECUTING(1, "此任务执行中");

    private final Integer code;

    private final String msg;

    TaskExecTypeEnums(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
