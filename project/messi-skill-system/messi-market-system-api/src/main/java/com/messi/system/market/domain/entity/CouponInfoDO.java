package com.messi.system.market.domain.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import com.messi.system.mybatis.BaseEntity;
import lombok.Data;

/**
 * 优惠券信息表
 */
@Data
@TableName("coupon_info")
public class CouponInfoDO extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 优惠券id
     */
    @TableField(value = "coupon_id", fill = FieldFill.DEFAULT)
    private String couponId;

    /**
     * 优惠券类型 0：满减优惠 1：折扣优惠
     */
    @TableField(value = "coupon_type", fill = FieldFill.DEFAULT)
    private Integer couponType;

    /**
     * 满减抵扣金额
     */
    @TableField(value = "coupon_price", fill = FieldFill.DEFAULT)
    private Integer couponPrice;

    /**
     * 折扣比率
     */
    @TableField(value = "coupon_discount", fill = FieldFill.DEFAULT)
    private Integer couponDiscount;

    /**
     * 分发的指定用户
     */
    @TableField(value = "user_id", fill = FieldFill.DEFAULT)
    private String userId;

    /**
     * 使用情况 0：未使用 1：已使用
     */
    @TableField(value = "use_status", fill = FieldFill.DEFAULT)
    private Integer useStatus;

    /**
     * 使用时间
     */
    @TableField(value = "usage_time", fill = FieldFill.DEFAULT)
    private Date usageTime;

}
