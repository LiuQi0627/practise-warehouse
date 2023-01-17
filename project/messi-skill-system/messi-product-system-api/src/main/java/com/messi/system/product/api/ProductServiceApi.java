package com.messi.system.product.api;

import com.messi.system.core.ResResult;
import com.messi.system.product.domain.dto.DeductionStockProductDTO;
import com.messi.system.product.domain.dto.SkuDTO;
import com.messi.system.product.domain.request.ProductReq;

import java.util.List;

/**
 * product service对外暴露的dubbo api接口
 */
public interface ProductServiceApi {

    /**
     * 检查商品
     *
     * @param productReqList 检查商品list
     * @return 检查结果
     */
    Boolean checkProduct(List<ProductReq> productReqList);

    /**
     * 查询sku
     *
     * @param productId 商品id
     * @param skuId     sku id
     * @return skuDTO
     */
    SkuDTO getSku(String productId, String skuId);

    /**
     * 扣除sku库存
     *
     * @param productReqList productReqList
     */
    void deductionStock(List<ProductReq> productReqList);

    /**
     * 单独扣减库存
     *
     * @param deductionStockProductDTO deductionStockProductDTO
     */
    void aloneDeductionStock(DeductionStockProductDTO deductionStockProductDTO);
}
