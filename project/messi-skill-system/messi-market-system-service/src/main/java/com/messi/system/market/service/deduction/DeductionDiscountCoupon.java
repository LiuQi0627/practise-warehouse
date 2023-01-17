package com.messi.system.market.service.deduction;

import com.messi.system.market.domain.dto.CouponDTO;
import org.springframework.stereotype.Component;

/**
 * 扣减折扣优惠券操作类
 */
@Component
public class DeductionDiscountCoupon extends DeductionCouponService {

    /**
     * 计算折扣优惠券能够抵扣的金额
     */
    public void calCouponDiscountAmount() {
        //  空实现
        //  可自定义业务
    }

    /**
     * 扣减折扣优惠券
     *
     * @param couponDTO 优惠券DTO
     */
    @Override
    public void deduction(CouponDTO couponDTO) {
        //  空实现
        //  可自定义业务
    }
}
