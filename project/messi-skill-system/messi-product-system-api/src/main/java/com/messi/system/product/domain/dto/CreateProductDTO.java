package com.messi.system.product.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建商品DTO
 */
@Data
public class CreateProductDTO implements Serializable {
    private static final long serialVersionUID = -4213675060010276117L;

    /**
     * 商品id
     */
    private String productId;

    /**
     * sku id
     */
    private String skuId;
}
