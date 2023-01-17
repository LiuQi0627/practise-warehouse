package com.messi.system.coupon;

import com.messi.system.market.MarketApp;
import com.messi.system.market.domain.dto.CouponDTO;
import com.messi.system.market.domain.entity.CouponInfoDO;
import com.messi.system.market.domain.request.CreateCouponReq;
import com.messi.system.market.service.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.LinkedList;
import java.util.List;

/**
 * 扣减优惠券测试类
 */
@SpringBootTest(classes = MarketApp.class)
@RunWith(SpringJUnit4ClassRunner.class)
@Slf4j
public class DeductionCouponTest {

    @Autowired
    private CouponService couponService;

    /**
     * 扣减满减优惠券
     */
    @Test
    public void deductionFullCoupon() {
        CouponDTO coupon = couponService.getCoupon("10001", "200");
        couponService.deductionCoupon(coupon);
    }

    /**
     * 模拟创建优惠券
     */
    @Test
    public void mockCreateCoupon() {
        List<CouponInfoDO> couponInfoDOList = new LinkedList<>();
        for (int i = 0; i < 10000; i++) {
            CouponInfoDO couponInfoDO = new CouponInfoDO();
            couponInfoDO.setCouponId(String.valueOf(1000 + i));
            couponInfoDO.setCouponType(0);
            couponInfoDO.setCouponPrice(1000);
            couponInfoDO.setUserId("USER" + i);
            couponInfoDO.setUseStatus(0);
            couponInfoDO.setUsageTime(null);

            couponInfoDOList.add(couponInfoDO);
        }

        couponService.mockCreateCoupon(couponInfoDOList);
        log.info("mockCreateCoupon执行完成");
    }
}
