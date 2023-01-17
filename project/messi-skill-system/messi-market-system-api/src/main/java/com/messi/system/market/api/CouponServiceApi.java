package com.messi.system.market.api;

import com.messi.system.market.domain.dto.CouponDTO;

/**
 * coupon service对外暴露的dubbo api接口
 */
public interface CouponServiceApi {

    /**
     * 计算满减金额
     *
     * @param couponDTO couponDTO
     * @return 满减金额
     */
    Integer getCouponDeductionPrice(CouponDTO couponDTO);

    /**
     * 获取优惠券
     *
     * @param couponId 优惠券id
     * @param userId   用户id
     * @return 优惠券
     */
    CouponDTO getCoupon(String couponId, String userId);

    /**
     * 扣减优惠券
     *
     * @param couponDTO couponDTO
     */
    void deductionCoupon(CouponDTO couponDTO);
}
