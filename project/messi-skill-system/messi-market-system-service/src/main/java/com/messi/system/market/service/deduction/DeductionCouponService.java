package com.messi.system.market.service.deduction;

import com.messi.system.market.domain.dto.CouponDTO;
import org.springframework.stereotype.Component;

/**
 * 扣减优惠券的抽象父类
 */
public abstract class DeductionCouponService {

    /**
     * 扣减优惠券父类
     *
     * @param couponDTO 优惠券DTO
     */
    public abstract void deduction(CouponDTO couponDTO);

}
