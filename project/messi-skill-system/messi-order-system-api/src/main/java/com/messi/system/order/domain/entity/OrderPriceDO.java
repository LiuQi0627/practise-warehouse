package com.messi.system.order.domain.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;

import com.messi.system.mybatis.BaseEntity;
import lombok.Data;

/**
 * 订单价格表
 */
@Data
@TableName("order_price")
public class OrderPriceDO extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 订单id
     */
    @TableField(value = "order_id", fill = FieldFill.DEFAULT)
    private String orderId;

    /**
     * 单笔订单总价格
     */
    @TableField(value = "order_total_price", fill = FieldFill.DEFAULT)
    private Integer orderTotalPrice;

}
