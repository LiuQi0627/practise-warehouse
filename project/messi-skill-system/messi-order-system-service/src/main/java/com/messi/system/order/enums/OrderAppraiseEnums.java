package com.messi.system.order.enums;

/**
 * 订单评价枚举常量
 */
public enum OrderAppraiseEnums {

    NO_RATED(0, "未评价"),

    RATED(1, "已评价");


    private final Integer code;

    private final String msg;

    OrderAppraiseEnums(Integer code, String msg) {
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
