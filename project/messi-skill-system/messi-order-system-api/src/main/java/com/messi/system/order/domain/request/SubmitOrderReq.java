package com.messi.system.order.domain.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 提交订单请求入参
 */
@Data
public class SubmitOrderReq implements Serializable {
    private static final long serialVersionUID = -1671382775715363323L;

    /**
     * 订单号
     */
    private String orderId;

    /**
     * 渠道标识
     */
    private Integer channel;

    /**
     * 订单类型
     */
    private Integer orderType;

    /**
     * 卖家id
     */
    private String sellerId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 订单总金额
     */
    private Integer totalAmount;

    /**
     * 实付金额
     */
    private Integer actualAmount;

    /**
     * 优惠券id
     */
    private String couponId;

    /**
     * 省编号
     */
    private String provinceNo;

    /**
     * 市编号
     */
    private String cityNo;

    /**
     * 区编号
     */
    private String areaNo;

    /**
     * 详细地址
     */
    private String detailAddress;

    /**
     * 收货人姓名
     */
    private String recipientName;

    /**
     * 收货人联系电话
     */
    private String recipientPhone;

    /**
     * 订单条目
     */
    private List<OrderItemReq> orderItemReqList;

    /**
     * 订单价格
     */
    private List<OrderItemPrice> orderItemPriceList;

    /**
     * 订单商品
     */
    private List<OrderProduct> orderProductList;

    /**
     * 订单条目信息
     */
    @Data
    public static class OrderItemReq implements Serializable {
        private static final long serialVersionUID = -2623390776099971749L;

        /**
         * 订单条目id
         */
        private String orderItemId;

        /**
         * 商品id
         */
        private String productId;

        /**
         * sku id
         */
        private String skuId;

        /**
         * 销售数量
         */
        private Integer saleNum;

        /**
         * sku 单价
         */
        private Integer skuPrice;
    }

    /**
     * 订单价格信息
     */
    @Data
    public static class OrderItemPrice implements Serializable {
        private static final long serialVersionUID = 4765003055160546888L;

        /**
         * 订单条目id
         */
        private String orderItemId;

        /**
         * sku id
         */
        private String skuId;

        /**
         * 销售价格 = 销售数量 * sku单价
         */
        private Integer salePrice;
    }

    /**
     * 订单商品信息
     */
    @Data
    public static class OrderProduct implements Serializable {
        private static final long serialVersionUID = 8893928760497811600L;

        /**
         * 订单条目id
         */
        private String orderItemId;

        /**
         * 商品id
         */
        private String productId;

        /**
         * sku id
         */
        private String skuId;

        /**
         * sku单价
         */
        private Integer skuPrice;

        /**
         * 购买数量
         */
        private Integer saleNum;

    }
}