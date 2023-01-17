package com.messi.system.order.enums;

/**
 * 订单状态枚举常量
 */
public enum OrderStatusEnums {

    NEW(10, "新创建"),

    CREATED(20, "未支付"),

    PAID(30, "已支付"),

    STORAGE(40, "已入库"),

    OUTBOUND(50, "已出库"),

    DELIVERING(60, "配送中"),

    SIGNED(70, "已签收"),

    CANCELED(80, "已取消"),

    INVALID(999, "订单失效");

    private final Integer code;

    private final String msg;

    OrderStatusEnums(Integer code, String msg) {
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
