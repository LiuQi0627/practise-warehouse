package com.messi.system.market.enums;

/**
 * 营销中心枚举常量
 */
public enum MarketCouponEnums {

    COUPON_IS_NOT_USED(0, "当前优惠券未使用"),

    COUPON_IS_USED(1, "当前优惠券已使用"),

    FULL_DISCOUNT_COUPON(0, "满减优惠券"),

    DISCOUNT_COUPON(1, "折扣优惠券"),

    GRANT_COUPON(2, "赠予优惠券");

    private final Integer code;

    private final String msg;

    MarketCouponEnums(Integer code, String msg) {
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
