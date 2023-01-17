package com.messi.system.product.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * product和sku的临时合并DO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductAndSkuDO implements Serializable {

    private static final long serialVersionUID = 6878992483886060721L;

    private SkuDO skuDO;

    private SkuStockDO skuStockDO;
}
