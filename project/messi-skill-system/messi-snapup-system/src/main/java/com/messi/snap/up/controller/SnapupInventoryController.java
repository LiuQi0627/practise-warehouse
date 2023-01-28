package com.messi.snap.up.controller;

import com.messi.sanp.up.domain.entity.SyncSkuStockRequest;
import com.messi.sanp.up.service.SnapUpInventoryService;
import com.messi.system.core.ResResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 抢购库存controller
 */
@RestController
@RequestMapping("/inventory")
public class SnapupInventoryController {

    @Autowired
    private SnapUpInventoryService snapUpInventoryService;

    @PostMapping("/inventoryShards")
    private ResResult<Boolean> inventoryShards(@RequestBody SyncSkuStockRequest syncSkuStockRequest) {
        return snapUpInventoryService.snapUpInventoryShards(
                syncSkuStockRequest.getPromotionId(), syncSkuStockRequest.getSkuId(),
                syncSkuStockRequest.getTotalPurchaseStock()
        );
    }

}
