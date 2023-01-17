package com.messi.system.product;

import com.messi.system.product.domain.request.ProductReq;
import com.messi.system.product.service.ProductService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(classes = ProductApp.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class SkuStockTest {

    @Autowired
    private ProductService productService;

    /**
     * 扣减sku库存
     */
    @Test
    public void deductionStock() {
        ProductReq productReq = new ProductReq();
        productReq.setProductId("001");
        productReq.setSkuId("10001");
        productReq.setSaleNum(5);

        List<ProductReq> productReqList = new ArrayList<>();
        productReqList.add(productReq);

        productService.deductionStock(productReqList);
    }
}
