package com.messi.system.market.service.deduction;

import com.messi.system.market.domain.dto.CouponDTO;
import org.springframework.stereotype.Component;

/**
 * 扣减赠予的优惠券
 */
@Component
public class DeductionGrantCoupon extends DeductionCouponService {

    /**
     * 扣减赠予的优惠券
     *
     * @param couponDTO 优惠券DTO
     */
    @Override
    public void deduction(CouponDTO couponDTO) {
        //  空实现
    }
}
