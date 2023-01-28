package com.messi.snap.up.enums;

/**
 * 抢购流程枚举类
 */
public enum SnapupProcessEnums {

    SNAPUP_PROCESS_1(1, "Process1", "校验用户合法性"),

    SNAPUP_PROCESS_2(2, "Process2", "检查抢购sku"),

    SNAPUP_PROCESS_3(3, "Process3", "扣减库存"),

    SNAPUP_PROCESS_4(4, "Process4", "生成抢购订单"),

    SNAPUP_PROCESS_5(5, "Process5", "返回抢购成功"),
    ;

    private final Integer code;

    private final String channel;

    private final String msg;

    SnapupProcessEnums(Integer code, String channel, String msg) {
        this.code = code;
        this.channel = channel;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getChannel() {
        return channel;
    }

    public String getMsg() {
        return msg;
    }
}
