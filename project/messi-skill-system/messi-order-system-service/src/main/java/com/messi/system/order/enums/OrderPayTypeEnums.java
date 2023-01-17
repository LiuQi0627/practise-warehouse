package com.messi.system.order.enums;

/**
 * 订单支付类型枚举常量
 */
public enum OrderPayTypeEnums {

    WECHAT(0, "微信"),

    ALIPAY(1, "支付宝"),

    UNION_PAY(2, "银联");

    private final Integer code;

    private final String msg;

    OrderPayTypeEnums(Integer code, String msg) {
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
