package com.messi.system.product;

import com.alibaba.fastjson.JSON;
import com.messi.system.product.domain.entity.SkuDO;
import com.messi.system.product.domain.request.CreateProductReq;
import com.messi.system.product.service.ProductService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.LinkedList;

@SpringBootTest(classes = ProductApp.class)
@RunWith(SpringJUnit4ClassRunner.class)
@SuppressWarnings(value = {"all"})
public class ProductApiTest {

    @Autowired
    private ProductService productService;

    @Test
    public void createProduct() {

        CreateProductReq createProductReq = JSON.parseObject(
                "{\n" +
                        "  \"productId\": \"001\",\n" +
                        "  \"skuName\": \"商品001-sku10001\",\n" +
                        "  \"skuPrice\": 1000\n" +
                        "}",
                CreateProductReq.class);

        CreateProductReq.SkuReq skuReq = JSON.parseObject(
                "{\n" + "    \"skuId\": \"10001\",\n" +
                        "    \"totalStock\": 100,\n" +
                        "    \"saledStock\": 0,\n" +
                        "    \"lockedStock\": 0\n" +
                        "  }",
                CreateProductReq.SkuReq.class
        );
        createProductReq.setSkuReq(skuReq);
        productService.createProduct(createProductReq);
    }

    //  测试：LinkedList和ArrayList插入速度的例子
    //  结论：LinkedList底层使用的链表，插入删除快，查询慢，本次耗时1毫秒
    //  ArrayList底层使用数组，查询快，插入删除慢，本次耗时2毫秒
    public static void main(String[] args) {
//        List<Object> arrayList = new ArrayList<>();
        LinkedList<Object> linkedList = new LinkedList<>();
        System.out.println("开始时间：" + new Date());
        for (int i = 0; i < 100; i++) {
            SkuDO skuDO = new SkuDO();
            skuDO.setProductId(String.valueOf(i));
//            arrayList.add(skuDO);
            linkedList.add(skuDO);
        }
        System.out.println("结束时间：" + new Date());
    }
}
