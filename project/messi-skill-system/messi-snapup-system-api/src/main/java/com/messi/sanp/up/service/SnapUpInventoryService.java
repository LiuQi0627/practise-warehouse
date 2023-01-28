package com.messi.sanp.up.service;

import com.messi.system.core.ResResult;

/**
 * 抢购库存service
 */
public interface SnapUpInventoryService {

    /**
     * 同步分配sku的抢购库存
     *
     * @param promotionId        促销活动id
     * @param skuId              skuid
     * @param totalPurchaseStock 抢购库存总量
     */
    ResResult<Boolean> snapUpInventoryShards(Long promotionId, Long skuId, Integer totalPurchaseStock);
}
