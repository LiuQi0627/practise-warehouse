package com.messi.system.order.remote;

import com.messi.system.product.domain.dto.DeductionStockProductDTO;
import com.messi.system.order.converter.OrderConverter;
import com.messi.system.order.domain.request.SubmitOrderReq;
import com.messi.system.product.api.ProductServiceApi;
import com.messi.system.product.domain.request.ProductReq;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 订单系统封装的 调用商品服务远程接口 的组件
 */
@Component
public class ProductRemote {

    @DubboReference(retries = 0)
    public ProductServiceApi productServiceApi;

    @Autowired
    private OrderConverter orderConverter;

    /**
     * 检查商品
     */
    public Boolean checkProduct(List<SubmitOrderReq.OrderProduct> orderProductList) {
        List<ProductReq> productReqList = orderConverter.orderProductList2CheckProductList(orderProductList);
        return productServiceApi.checkProduct(productReqList);
    }

    /**
     * 库存sku库存
     */
    public void deductionStock(List<SubmitOrderReq.OrderProduct> orderProductList) {
        List<ProductReq> productReqList = orderConverter.orderProductList2ProductList(orderProductList);
        productServiceApi.deductionStock(productReqList);
    }

    /**
     * 单独库存sku库存
     */
    public void aloneDeductionStock(SubmitOrderReq.OrderProduct orderProduct) {
        DeductionStockProductDTO deductionStockProductDTO = orderConverter.orderProduct2DeductionStockProductDTO(orderProduct);
        productServiceApi.aloneDeductionStock(deductionStockProductDTO);
    }
}
