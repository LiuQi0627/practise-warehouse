package com.messi.system.consistency.enums;

/**
 * 最终一致性任务的执行状态常量枚举
 */
public enum TaskExecStatusEnums {
    /**
     * 新创建
     */
    NEW(0, "新建"),

    /**
     * 正在运行，可能成功，也可能失败
     */
    RUNNING(1, "运行中"),

    /**
     * 执行失败
     */
    FAILED(2, "执行失败"),

    /**
     * 执行成功
     */
    SUCCESS(3, "执行成功");

    private final Integer code;

    private final String msg;

    TaskExecStatusEnums(Integer code, String msg) {
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
