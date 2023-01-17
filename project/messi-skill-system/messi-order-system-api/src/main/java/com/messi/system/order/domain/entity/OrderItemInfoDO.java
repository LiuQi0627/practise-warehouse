package com.messi.system.order.domain.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;

import com.messi.system.mybatis.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单明细表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("order_item_info")
public class OrderItemInfoDO extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 所属订单id
     */
    @TableField(value = "order_id", fill = FieldFill.DEFAULT)
    private String orderId;

    /**
     * 订单条目id
     */
    @TableField(value = "order_item_id", fill = FieldFill.DEFAULT)
    private String orderItemId;

    /**
     * 商品id
     */
    @TableField(value = "product_id", fill = FieldFill.DEFAULT)
    private String productId;

    /**
     * skuid
     */
    @TableField(value = "sku_id", fill = FieldFill.DEFAULT)
    private String skuId;

    /**
     * 销售数量
     */
    @TableField(value = "sale_num", fill = FieldFill.DEFAULT)
    private Integer saleNum;

    /**
     * 销售单价，单位：分
     */
    @TableField(value = "sale_price", fill = FieldFill.DEFAULT)
    private Integer salePrice;

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

}
