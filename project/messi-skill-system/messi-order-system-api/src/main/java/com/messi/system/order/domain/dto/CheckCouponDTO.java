package com.messi.system.order.domain.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 订单系统检查优惠券DTO
 */
@Data
public class CheckCouponDTO implements Serializable {
    private static final long serialVersionUID = 3268814870971139971L;

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

    /**
     * 使用时间
     */
    private Date usageTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date modifiedTime;
}
