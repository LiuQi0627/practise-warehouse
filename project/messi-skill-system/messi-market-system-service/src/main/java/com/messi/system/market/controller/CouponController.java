package com.messi.system.market.controller;

import com.messi.system.lock.DistributedLock;
import com.messi.system.market.domain.request.CreateCouponReq;
import com.messi.system.market.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 优惠券controller
 */
@RestController
@RequestMapping("/coupon")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @Autowired
    private DistributedLock distributedLock;

    @PostMapping("/create")
    public void createProduct(@RequestBody CreateCouponReq createCouponReq) {
        couponService.createCoupon(createCouponReq);
    }
}
