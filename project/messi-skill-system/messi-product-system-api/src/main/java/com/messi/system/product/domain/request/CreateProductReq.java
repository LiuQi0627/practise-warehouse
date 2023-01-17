package com.messi.system.product.domain.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建商品入参
 */
@Data
public class CreateProductReq implements Serializable {
    private static final long serialVersionUID = -3125961655638214845L;

    /**
     * 商品id
     */
    private String productId;

    /**
     * sku名称
     */
    private String skuName;

    /**
     * sku单价
     */
    private Integer skuPrice;

    /**
     * sku
     */
    private SkuReq skuReq;

    /**
     * sku信息
     */
    @Data
    public static class SkuReq implements Serializable {
        private static final long serialVersionUID = -3668196039367305384L;

        /**
         * sku id
         */
        private String skuId;

        //  总库存 = 已售库存 + 已锁定库存
        /**
         * 总库存
         */
        private Integer totalStock;

        /**
         * 已售库存
         */
        private Integer saledStock;

        /**
         * 已锁定库存
         */
        private Integer lockedStock;
    }
}
