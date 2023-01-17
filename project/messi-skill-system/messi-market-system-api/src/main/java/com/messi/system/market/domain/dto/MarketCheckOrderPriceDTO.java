package com.messi.system.market.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 营销中心计算的订单价格
 */
@Data
public class MarketCheckOrderPriceDTO implements Serializable {
    private static final long serialVersionUID = -5508387017735499681L;

    /**
     * sku id
     */
    private String skuId;

    /**
     * 计算后的价格 = 单价 * 数量
     */
    private Integer calculatePrice;

    public MarketCheckOrderPriceDTO(String skuId, Integer calculatePrice) {
        this.skuId = skuId;
        this.calculatePrice = calculatePrice;
    }
}
