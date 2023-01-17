package com.messi.system.product.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 检查商品入参
 */
@Data
public class ProductReq implements Serializable {
    private static final long serialVersionUID = 2576474701587314511L;

    /**
     * 商品id
     */
    private String productId;

    /**
     * sku id
     */
    private String skuId;

    /**
     * 购买数量
     */
    private Integer saleNum;

}
