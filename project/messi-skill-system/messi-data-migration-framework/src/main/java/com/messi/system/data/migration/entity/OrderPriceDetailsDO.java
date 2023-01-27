package com.messi.system.data.migration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 订单价格明细表
 */
@Data
@TableName("order_price_details")
public class OrderPriceDetailsDO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 订单id
     */
    private String orderId;

    /**
     * 订单条目id
     */
    private String orderItemId;

    /**
     * sku id
     */
    private String skuId;

    /**
     * 单笔条目sku销售数量
     */
    private Integer saleNum;

    /**
     * sku销售原价
     */
    private Integer salePrice;

    /**
     * 订单条目实际收费价格
     */
    private Integer orderItemPrice;

    /**
     * 主键
     */
    private Long id;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date modifiedTime;

}
