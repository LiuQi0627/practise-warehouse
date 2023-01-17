package com.messi.system.order.elasticsearch.index;

import com.alibaba.fastjson.annotation.JSONField;
import com.messi.system.order.elasticsearch.annotation.EsDocument;
import com.messi.system.order.elasticsearch.annotation.EsField;
import com.messi.system.order.elasticsearch.annotation.EsId;
import com.messi.system.order.elasticsearch.constants.EsDataTypeConstants;
import com.messi.system.order.elasticsearch.enums.EsIndexEnums;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 订单es数据index
 * 需要从es中查询的数据都在这里做成索引，避免去联表查询数据库
 * 这里就是一张大宽表
 */
@Data
@EsDocument(index = EsIndexEnums.ORDER_ES_INDEX)
public class OrderEsIndex implements Serializable {
    private static final long serialVersionUID = -1314896656609703911L;

    /**
     * es 索引id
     */
    @EsId
    @EsField(type = EsDataTypeConstants.KEYWORD)
    private String esId;

    /**
     * 渠道标识 0:C端渠道，999:其他
     */
    @EsField(type = EsDataTypeConstants.INTEGER)
    private Integer channel;

    /**
     * 订单号
     */
    @EsField(type = EsDataTypeConstants.KEYWORD)
    private String orderId;

    /**
     * 订单类型 0：标准订单 999：其他
     */
    @EsField(type = EsDataTypeConstants.INTEGER)
    private Integer orderType;

    /**
     * 00：未支付,10：已支付，20：已入库，30：已出库，40：配送中：50：已签收，60：已取消，999：订单失效
     * 注：省略已入库->已出库中间的履约流程
     */
    @EsField(type = EsDataTypeConstants.INTEGER)
    private Integer orderStatus;

    /**
     * 取消订单的时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss.SSS")
    @EsField(type = EsDataTypeConstants.DATE)
    private Date orderCancelTime;

    /**
     * 卖家id
     */
    @EsField(type = EsDataTypeConstants.KEYWORD)
    private String sellerId;

    /**
     * 买家id
     */
    @EsField(type = EsDataTypeConstants.KEYWORD)
    private String userId;

    /**
     * 订单总金额，单位：分
     */
    @EsField(type = EsDataTypeConstants.INTEGER)
    private Integer totalAmount;

    /**
     * 实付金额，单位：分
     */
    @EsField(type = EsDataTypeConstants.INTEGER)
    private Integer actualAmount;

    /**
     * 订单支付类型 0：微信 1：支付宝  2：银联
     */
    @EsField(type = EsDataTypeConstants.INTEGER)
    private Integer orderPayType;

    /**
     * 支付时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @EsField(type = EsDataTypeConstants.DATE)
    private Date payTime;

    /**
     * 优惠券id
     */
    @EsField(type = EsDataTypeConstants.KEYWORD)
    private String couponId;

    /**
     * 订单评价状态 0：未评价 1：已评价
     */
    @EsField(type = EsDataTypeConstants.INTEGER)
    private Integer appraiseStatus;

    /**
     * 订单条目
     */
    @EsField(type = EsDataTypeConstants.OBJECT)
    private List<OrderEsIndex.OrderItemDTO> orderItemDTOs;

    /**
     * 单笔订单总价格
     */
    @EsField(type = EsDataTypeConstants.INTEGER)
    private Integer orderTotalPrice;

    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @EsField(type = EsDataTypeConstants.DATE)
    private Date createTime;

    /**
     * 修改时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @EsField(type = EsDataTypeConstants.DATE)
    private Date modifiedTime;

    @Data
    public static class OrderItemDTO implements Serializable {
        private static final long serialVersionUID = -9181831005834605287L;

        /**
         * 订单条目id
         */
        @EsField(type = EsDataTypeConstants.KEYWORD)
        private String orderItemId;

        /**
         * 商品id
         */
        @EsField(type = EsDataTypeConstants.KEYWORD)
        private String productId;

        /**
         * skuid
         */
        @EsField(type = EsDataTypeConstants.KEYWORD)
        private String skuId;

        /**
         * 销售数量
         */
        @EsField(type = EsDataTypeConstants.INTEGER)
        private Integer saleNum;

        /**
         * 销售单价，单位：分
         */
        @EsField(type = EsDataTypeConstants.INTEGER)
        private Integer salePrice;

        /**
         * 订单条目实际收费价格
         */
        @EsField(type = EsDataTypeConstants.INTEGER)
        private Integer orderItemPrice;

    }

}
