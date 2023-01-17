package com.messi.system.order.domain.entity;

import com.baomidou.mybatisplus.annotation.*;


import java.io.Serializable;
import java.util.Date;

import com.messi.system.mybatis.BaseEntity;
import lombok.Data;

/**
 * 订单信息表
 */
@Data
@TableName("order_info")
public class OrderInfoDO extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 渠道标识 0:C端渠道，999:其他
     */
    @TableField(value = "channel", fill = FieldFill.DEFAULT)
    private Integer channel;

    /**
     * 订单号
     */
    @TableField(value = "order_id", fill = FieldFill.DEFAULT)
    private String orderId;

    /**
     * 订单类型 0：标准订单 999：其他
     */
    @TableField(value = "order_type", fill = FieldFill.DEFAULT)
    private Integer orderType;

    /**
     * 00：未支付,10：已支付，20：已入库，30：已出库，40：配送中：50：已签收，60：已取消，999：订单失效
     * 注：省略已入库->已出库中间的履约流程
     */
    @TableField(value = "order_status", fill = FieldFill.DEFAULT)
    private Integer orderStatus;

    /**
     * 取消订单的时间
     */
    @TableField(value = "order_cancel_time", fill = FieldFill.DEFAULT)
    private Date orderCancelTime;

    /**
     * 卖家id
     */
    @TableField(value = "seller_id", fill = FieldFill.DEFAULT)
    private String sellerId;

    /**
     * 买家id
     */
    @TableField(value = "user_id", fill = FieldFill.DEFAULT)
    private String userId;

    /**
     * 订单总金额，单位：分
     */
    @TableField(value = "total_amount", fill = FieldFill.DEFAULT)
    private Integer totalAmount;

    /**
     * 实付金额，单位：分
     */
    @TableField(value = "actual_amount", fill = FieldFill.DEFAULT)
    private Integer actualAmount;

    /**
     * 订单支付类型 0：微信 1：支付宝  2：银联
     */
    @TableField(value = "order_pay_type", fill = FieldFill.DEFAULT)
    private Integer orderPayType;

    /**
     * 支付时间
     */
    @TableField(value = "pay_time", fill = FieldFill.DEFAULT)
    private Date payTime;

    /**
     * 优惠券id
     */
    @TableField(value = "coupon_id", fill = FieldFill.DEFAULT)
    private String couponId;

    /**
     * 订单评价状态 0：未评价 1：已评价
     */
    @TableField(value = "appraise_status", fill = FieldFill.DEFAULT)
    private Integer appraiseStatus;

}
