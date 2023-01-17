package com.messi.system.constant;

/**
 * rocketmq 常量类
 */
public class RocketMqConstants {

    /**
     * 默认的producer分组
     */
    public static final String DEFAULT_PRODUCER_GROUP = "default_producer_group";

    /**
     * 默认的transaction producer分组
     */
    public static final String DEFAULT_TRANSACTION_PRODUCER_GROUP = "default_transaction_producer_group";

    /**
     * 支付前取消订单 topic
     */
    public static final String NOT_PAID_CANCEL_ORDER_TOPIC = "not_paid_cancel_order_topic";

    /**
     * 支付前取消订单 group
     */
    public static final String NOT_PAID_CANCEL_ORDER_GROUP = "not_paid_cancel_order_group";

    /**
     * 支付前取消订单扣减事务消息 topic
     */
    public static final String NOT_PAID_CANCEL_ORDER_DEDUCTION_TRANSACTION_TOPIC = "not_paid_cancel_order_deduction_transaction_topic";

    /**
     * 扣减优惠券 transaction group
     */
    public static final String DEDUCTION_COUPON_TRANSACTION_GROUP = "deduction_coupon_transaction_group";

    /**
     * 扣减库存 transaction group
     */
    public static final String DEDUCTION_STOCK_TRANSACTION_GROUP = "deduction_stock_transaction_group";

    /**
     * 扣减优惠券 topic
     */
    public static final String DEDUCTION_COUPON_TOPIC = "deduction_coupon_topic";

    /**
     * 扣减库存 topic
     */
    public static final String DEDUCTION_STOCK_TOPIC = "deduction_stock_topic";

}
