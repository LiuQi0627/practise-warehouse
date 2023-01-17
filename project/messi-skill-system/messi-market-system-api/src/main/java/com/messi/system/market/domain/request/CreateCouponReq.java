package com.messi.system.market.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建优惠券入参
 */
@Data
public class CreateCouponReq implements Serializable {
    private static final long serialVersionUID = 2622927862243531782L;

    /**
     * 优惠券id
     */
    private String couponId;

    /**
     * 优惠券类型 0：满减优惠 1：折扣优惠
     */
    private Integer couponType;

    /**
     * 满减抵扣金额
     */
    private Integer couponPrice;

    /**
     * 折扣比率
     */
    private Integer couponDiscount;

    /**
     * 分发的指定用户
     */
    private String userId;

    /**
     * 使用情况 0：未使用 1：已使用
     */
    private Integer useStatus;

}
