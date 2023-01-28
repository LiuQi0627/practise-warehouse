package com.messi.sanp.up.domain.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * sku库存分片的请求入参
 */
@Data
public class SyncSkuStockRequest implements Serializable {
    private static final long serialVersionUID = 1673444619822765825L;

    /**
     * 已审核通过后的促销活动id
     */
    private Long promotionId;

    /**
     * sku id
     */
    private Long skuId;

    /**
     * 总抢购库存
     */
    private Integer totalPurchaseStock;
}
