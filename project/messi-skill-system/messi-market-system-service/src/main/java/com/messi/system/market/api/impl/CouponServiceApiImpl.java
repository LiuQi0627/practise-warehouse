package com.messi.system.market.api.impl;

import com.messi.system.market.api.CouponServiceApi;
import com.messi.system.market.domain.dto.CouponDTO;
import com.messi.system.market.service.CouponService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 优惠券 dubbo api 实现类
 */
@DubboService
public class CouponServiceApiImpl implements CouponServiceApi {

    @Autowired
    private CouponService couponService;

    /**
     * 计算满减金额
     *
     * @param couponDTO couponDTO
     * @return 满减金额
     */
    @Override
    public Integer getCouponDeductionPrice(CouponDTO couponDTO) {
        return couponService.getCouponFullDeductionPrice(couponDTO);
    }

    /**
     * 获取优惠券
     *
     * @param couponId 优惠券id
     * @param userId   用户id
     * @return 优惠券
     */
    @Override
    public CouponDTO getCoupon(String couponId, String userId) {
        return couponService.getCoupon(couponId, userId);
    }

    /**
     * 扣减优惠券
     *
     * @param couponDTO couponDTO
     */
    @Override
    public void deductionCoupon(CouponDTO couponDTO) {
        couponService.deductionCoupon(couponDTO);
    }
}
