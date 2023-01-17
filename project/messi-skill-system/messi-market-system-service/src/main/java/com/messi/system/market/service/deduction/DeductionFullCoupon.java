package com.messi.system.market.service.deduction;

import com.messi.system.constant.DistributedLockConstants;
import com.messi.system.lock.DistributedLock;
import com.messi.system.market.dao.CouponDAO;
import com.messi.system.market.domain.dto.CouponDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 扣减满减优惠券
 */
@Slf4j
@Component
public class DeductionFullCoupon extends DeductionCouponService {

    @Autowired
    private DistributedLock distributedLock;

    @Autowired
    private CouponDAO couponDAO;

    /**
     * 扣减满减优惠券
     *
     * @param couponDTO 优惠券DTO
     */
    @Override
    public void deduction(CouponDTO couponDTO) {
        //  分布式锁
        String redisKey = distributedLock(couponDTO.getCouponId());

        try {
            String couponId = couponDTO.getCouponId();
            String userId = couponDTO.getUserId();

            couponDAO.updateCouponUseStatus(couponId, userId);
            log.info("扣减满减优惠券成功,couponId:{},userId:{}", couponId, userId);

        } finally {
            distributedLock.unLock(redisKey);
        }
    }

    private String distributedLock(String couponId) {
        String redisKey = DistributedLockConstants.DEDUCTION_FULL_COUPON + couponId;
        if (!distributedLock.tryLock(redisKey)) {
            log.warn("扣减满减优惠券加锁失败");
            throw new RuntimeException("扣减满减优惠券加锁失败");
        }
        return redisKey;
    }
}
