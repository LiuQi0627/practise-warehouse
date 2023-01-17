package com.messi.system.market.remote;

import com.messi.system.product.api.ProductServiceApi;
import com.messi.system.product.domain.dto.SkuDTO;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/**
 * 营销系统封装的 调用商品服务远程接口 的组件
 */
@Component
public class ProductRemote {

    @DubboReference(retries = 0)
    public ProductServiceApi productServiceApi;

    /**
     * 查询sku
     *
     * @param productId 商品id
     * @param skuId     sku id
     * @return skuDTO
     */
    public SkuDTO getSku(String productId, String skuId) {
        return productServiceApi.getSku(productId, skuId);
    }
}
