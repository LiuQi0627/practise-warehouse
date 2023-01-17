package com.messi.system.order.domain.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 订单详情DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailDTO implements Serializable {
    private static final long serialVersionUID = 8874515073935183457L;

    /**
     * 渠道标识 0:C端渠道，999:其他
     */
    private Integer channel;

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
    @JSONField(format = "yyyy-MM-dd HH:mm:ss.SSS")
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
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date payTime;

    /**
     * 优惠券id
     */
    private String couponId;

    /**
     * 订单评价状态 0：未评价 1：已评价
     */
    private Integer appraiseStatus;

    /**
     * 订单条目
     */
    private List<OrderDetailDTO.OrderItemDTO> orderItemDTOs;

    /**
     * 单笔订单总价格
     */
    private Integer orderTotalPrice;


    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 修改时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date modifiedTime;

    @Data
    public static class OrderItemDTO implements Serializable {
        private static final long serialVersionUID = 6564981787733058533L;

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

    }
}
