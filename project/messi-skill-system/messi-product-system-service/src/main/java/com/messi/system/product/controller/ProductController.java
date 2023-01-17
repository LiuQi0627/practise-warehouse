package com.messi.system.product.controller;

import com.messi.system.constant.DistributedLockConstants;
import com.messi.system.lock.DistributedLock;
import com.messi.system.product.domain.request.CreateProductReq;
import com.messi.system.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 商品controller
 */
@RestController
@RequestMapping("/product")
@Slf4j
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping("/create")
    public void createProduct(@RequestBody CreateProductReq createProductReq) {
        productService.createProduct(createProductReq);
    }
}
