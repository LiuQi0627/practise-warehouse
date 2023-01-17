package com.messi.system.order.domain.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;

import com.messi.system.mybatis.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单价格明细表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("order_price_details")
public class OrderPriceDetailsDO extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 订单id
     */
    @TableField(value = "order_id", fill = FieldFill.DEFAULT)
    private String orderId;

    /**
     * 订单条目id
     */
    @TableField(value = "order_item_id", fill = FieldFill.DEFAULT)
    private String orderItemId;

    /**
     * sku id
     */
    @TableField(value = "sku_id", fill = FieldFill.DEFAULT)
    private String skuId;

    /**
     * 单笔条目sku销售数量
     */
    @TableField(value = "sale_num", fill = FieldFill.DEFAULT)
    private Integer saleNum;

    /**
     * sku销售原价
     */
    @TableField(value = "sale_price", fill = FieldFill.DEFAULT)
    private Integer salePrice;

    /**
     * 订单条目实际收费价格
     */
    @TableField(value = "order_item_price", fill = FieldFill.DEFAULT)
    private Integer orderItemPrice;

}
