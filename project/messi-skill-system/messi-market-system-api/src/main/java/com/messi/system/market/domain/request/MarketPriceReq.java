package com.messi.system.market.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 营销中心计算订单价格入参
 */
@Data
public class MarketPriceReq implements Serializable {
    private static final long serialVersionUID = -2754350139545392012L;

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
