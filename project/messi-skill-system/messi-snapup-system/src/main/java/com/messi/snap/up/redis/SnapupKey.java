package com.messi.snap.up.redis;

/**
 * 抢购关联的常量和默认方法
 */
public interface SnapupKey {
    /**
     * 可售库存
     */
    String SALABLE_STOCK = "salableStock";

    /**
     * 已锁定库存
     */
    String LOCKED_STOCK = "lockedStock";

    /**
     * 已售库存
     */
    String SOLD_STOCK = "soldStock";

    String PREFIX_SNAP_UP_STOCK = "snap_up_stock:";

    static String buildStockKey(Long promotionId, String skuId) {
        return PREFIX_SNAP_UP_STOCK + promotionId + ":" + skuId;
    }

    static String buildCheckSkuKey(Long promotionId, String skuId, String userId) {
        return "checkSku:" + promotionId + ":" + userId + ":" + skuId;
    }

}
