package com.messi.system.market.service;

import com.messi.system.market.domain.dto.CouponDTO;
import com.messi.system.market.domain.entity.CouponInfoDO;
import com.messi.system.market.domain.request.CreateCouponReq;

import java.util.LinkedList;
import java.util.List;

/**
 * 优惠券 service
 */
public interface CouponService {

    /**
     * 计算满减金额
     *
     * @param couponDTO couponDTO
     * @return 满减金额
     */
    Integer getCouponFullDeductionPrice(CouponDTO couponDTO);

    /**
     * 获取优惠券
     *
     * @param couponId 优惠券id
     * @param userId   用户id
     * @return 优惠券
     */
    CouponDTO getCoupon(String couponId, String userId);

    /**
     * 创建优惠券
     * 简化创建过程
     *
     * @param createCouponReq 优惠券入参
     */
    void createCoupon(CreateCouponReq createCouponReq);

    /**
     * 扣减优惠券
     *
     * @param couponDTO couponDTO
     */
    void deductionCoupon(CouponDTO couponDTO);

    /**
     * 模拟创建优惠券
     *
     * @param couponInfoDOList 模拟优惠券数据list
     */
    void mockCreateCoupon(List<CouponInfoDO> couponInfoDOList);
}
