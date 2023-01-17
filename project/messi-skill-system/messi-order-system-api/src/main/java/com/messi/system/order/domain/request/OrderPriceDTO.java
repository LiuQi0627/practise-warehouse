package com.messi.system.order.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 订单金额信息DTO
 */
@Data
public class OrderPriceDTO implements Serializable {
    private static final long serialVersionUID = 4849168454273004834L;

    /**
     * 订单id
     */
    private String orderId;

    /**
     * 单笔订单总价
     */
    private Integer orderTotalPrice;

    /**
     * 订单明细价格
     */
    @Data
    public static class OrderPriceDetails implements Serializable {
        private static final long serialVersionUID = 4047389972744102468L;

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
    }
}
