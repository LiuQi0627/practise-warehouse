package com.messi.system.data.migration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 *
 */
@Data
@TableName("order_item_info")
public class OrderItemInfoDO implements Serializable {
    private static final long serialVersionUID = 8213630794163483719L;

    /**
     * 所属订单id
     */
    private String orderId;

    /**
     * 订单条目id
     */
    private String orderItemId;

    /**
     * 商品id
     */
    private String productId;

    /**
     * skuid
     */
    private String skuId;

    /**
     * 销售数量
     */
    private Integer saleNum;

    /**
     * 销售单价，单位：分
     */
    private Integer salePrice;

    /**
     * 卖家id
     */
    private String sellerId;

    /**
     * 买家id
     */
    private String userId;

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
