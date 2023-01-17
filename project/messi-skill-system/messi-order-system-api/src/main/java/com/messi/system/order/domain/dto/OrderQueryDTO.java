package com.messi.system.order.domain.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 查询订单分页结果DTO
 */
@Data
public class OrderQueryDTO implements Serializable {
    private static final long serialVersionUID = -361681502992729247L;

    /**
     * 订单号
     */
    private String orderId;

    /**
     * 订单类型 0：标准订单 999：其他
     */
    private Integer orderType;

    /**
     * 00：未支付,10：已支付，20：已入库，30：已出库，40：配送中：50：已签收，60：已取消，999：订单失效
     * 注：省略已入库->已出库中间的履约流程
     */
    private Integer orderStatus;

    /**
     * 取消订单的时间
     */
    private Date orderCancelTime;

    /**
     * 卖家id
     */
    private String sellerId;

    /**
     * 买家id
     */
    private String userId;

    /**
     * 订单总金额，单位：分
     */
    private Integer totalAmount;

    /**
     * 实付金额，单位：分
     */
    private Integer actualAmount;

    /**
     * 订单支付类型 0：微信 1：支付宝  2：银联
     */
    private Integer orderPayType;

    /**
     * 支付时间
     */
    private Date payTime;

    /**
     * 优惠券id
     */
    private String couponId;

    /**
     * 渠道标识 0:C端渠道，999:其他
     */
    private Integer channel;

    /**
     * 订单评价状态 0：未评价 1：已评价
     */
    private Integer appraiseStatus;

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
     * 订单条目实际收费价格
     */
    private Integer orderItemPrice;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date modifiedTime;
}
