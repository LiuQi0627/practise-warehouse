package com.messi.system.market.service.deduction.factory;

import com.messi.system.market.domain.dto.CouponDTO;
import com.messi.system.market.enums.MarketCouponEnums;
import com.messi.system.market.service.deduction.DeductionDiscountCoupon;
import com.messi.system.market.service.deduction.DeductionFullCoupon;
import com.messi.system.market.service.deduction.DeductionGrantCoupon;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 扣减优惠券的执行工厂
 */
@Component
@Slf4j
public class DeductionCouponFactory {

    @Autowired
    private DeductionFullCoupon deductionFullCoupon;

    @Autowired
    private DeductionDiscountCoupon deductionDiscountCoupon;

    @Autowired
    private DeductionGrantCoupon deductionGrantCoupon;

    /**
     * 分配扣减优惠券执行器
     *
     * @param couponDTO couponDTO
     */
    public void deduction(CouponDTO couponDTO) {
        if (couponDTO == null) {
            log.info("couponDTO是空,不做任何操作");
            return;
        }

        Integer couponType = couponDTO.getCouponType();

        //  满减类型的优惠券
        if (MarketCouponEnums.FULL_DISCOUNT_COUPON.getCode().equals(couponType)) {
            deductionFullCoupon.deduction(couponDTO);

            //  折扣类型的优惠券
        } else if (MarketCouponEnums.DISCOUNT_COUPON.getCode().equals(couponType)) {
            deductionDiscountCoupon.calCouponDiscountAmount();
            deductionDiscountCoupon.deduction(couponDTO);

            //  赠予类型的优惠券
        } else if (MarketCouponEnums.GRANT_COUPON.getCode().equals(couponType)) {
            deductionGrantCoupon.deduction(couponDTO);
        }

    }
}
