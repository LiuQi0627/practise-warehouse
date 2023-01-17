package com.messi.system.product.service;

import com.messi.system.core.ResResult;
import com.messi.system.product.domain.dto.DeductionStockProductDTO;
import com.messi.system.product.domain.dto.SkuDTO;
import com.messi.system.product.domain.request.ProductReq;
import com.messi.system.product.domain.request.CreateProductReq;

import java.util.List;

/**
 * 商品service
 */
public interface ProductService {

    /**
     * 创建商品
     *
     * @param createProductReq 商品入参
     */
    void createProduct(CreateProductReq createProductReq);

    /**
     * 获取sku
     *
     * @param skuId sku id
     * @return 响应结果
     */
    SkuDTO getSku(String productId, String skuId);

    /**
     * 检查商品
     *
     * @param productReqList 检查商品list
     * @return 检查结果
     */
    Boolean checkProduct(List<ProductReq> productReqList);

    /**
     * 扣除sku库存
     *
     * @param productReqList productReqList
     */
    void deductionStock(List<ProductReq> productReqList);

    /**
     * 单独扣减sku库存
     *
     * @param deductionStockProductDTO deductionStockProductDTO
     */
    void aloneDeductionStock(DeductionStockProductDTO deductionStockProductDTO);

    /**
     * 提供给事务消息使用的，补偿库存
     *
     * @param productReqList productReqList
     */
    Boolean compensationStock(List<ProductReq> productReqList);

}
