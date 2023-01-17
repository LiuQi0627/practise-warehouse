package com.messi.system.product.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 扣减库存使用的商品DTO
 */
@Data
public class DeductionStockProductDTO implements Serializable {
    private static final long serialVersionUID = 192308256903672771L;

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
