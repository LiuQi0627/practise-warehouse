package com.messi.system.product.api.impl;

import com.messi.system.product.api.ProductServiceApi;
import com.messi.system.product.domain.dto.DeductionStockProductDTO;
import com.messi.system.product.domain.dto.SkuDTO;
import com.messi.system.product.domain.request.ProductReq;
import com.messi.system.product.service.ProductService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 商品 dubbo api 实现类
 */
@DubboService
public class ProductServiceApiImpl implements ProductServiceApi {

    @Autowired
    private ProductService productService;

    /**
     * 检查商品
     *
     * @param productReqList 检查商品list
     * @return 检查结果
     */
    @Override
    public Boolean checkProduct(List<ProductReq> productReqList) {
        return productService.checkProduct(productReqList);
    }

    /**
     * 查询sku
     *
     * @param productId 商品id
     * @param skuId     sku id
     * @return skuDTO
     */
    @Override
    public SkuDTO getSku(String productId, String skuId) {
        return productService.getSku(productId, skuId);
    }

    /**
     * 扣除sku库存
     *
     * @param productReqList productReqList
     */
    @Override
    public void deductionStock(List<ProductReq> productReqList) {
        productService.deductionStock(productReqList);
    }

    /**
     * 单独扣减库存
     *
     * @param deductionStockProductDTO deductionStockProductDTO
     */
    @Override
    public void aloneDeductionStock(DeductionStockProductDTO deductionStockProductDTO) {
        productService.aloneDeductionStock(deductionStockProductDTO);
    }
}
