package com.messi.system.product.domain.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 *
 */
@Data
public class SkuDTO implements Serializable {
    private static final long serialVersionUID = -9199106792250846854L;

    /**
     * 商品id
     */
    private String productId;

    /**
     * skuid
     */
    private String skuId;

    /**
     * 商品名称
     */
    private String skuName;

    /**
     * sku单价 单位：分
     */
    private Integer skuPrice;
}
