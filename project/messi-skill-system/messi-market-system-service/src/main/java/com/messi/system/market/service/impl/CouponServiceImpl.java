package com.messi.system.market.service.impl;

import com.messi.system.market.converter.CouponConverter;
import com.messi.system.market.dao.CouponDAO;
import com.messi.system.market.domain.dto.CouponDTO;
import com.messi.system.market.domain.entity.CouponInfoDO;
import com.messi.system.market.domain.request.CreateCouponReq;
import com.messi.system.market.enums.MarketCouponEnums;
import com.messi.system.market.service.CouponService;
import com.messi.system.market.service.deduction.factory.DeductionCouponFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 优惠券 service实现类
 */
@Slf4j
@Service
public class CouponServiceImpl implements CouponService {

    @Autowired
    private CouponDAO couponDAO;

    @Autowired
    private CouponConverter couponConverter;

    @Autowired
    private DeductionCouponFactory deductionCouponFactory;

    /**
     * 计算满减金额
     *
     * @param couponDTO couponDTO
     * @return 满减金额
     */
    @Override
    public Integer getCouponFullDeductionPrice(CouponDTO couponDTO) {
        CouponInfoDO couponInfo = couponConverter.couponDTO2DO(couponDTO);

        //  检查优惠券
        checkFullDeductionCoupon(couponInfo);

        //  返回满减金额
        return couponInfo.getCouponPrice();
    }

    private void checkFullDeductionCoupon(CouponInfoDO couponInfo) {
        //  优惠券已使用
        Integer useStatus = couponInfo.getUseStatus();
        Date usageTime = couponInfo.getUsageTime();
        if (MarketCouponEnums.COUPON_IS_USED.getCode().equals(useStatus) || usageTime != null) {
            // todo: 2023/1/17 为了方便压测，先注释掉合理的业务校验
//            throw new RuntimeException(MarketCouponEnums.COUPON_IS_USED.getMsg());
        }

        //  优惠券是折扣券
        Integer couponType = couponInfo.getCouponType();
        if (MarketCouponEnums.DISCOUNT_COUPON.getCode().equals(couponType)) {
            throw new RuntimeException("优惠券类型错误，当前优惠券是折扣优惠券");
        }
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
        CouponInfoDO couponInfoDO = couponDAO.getCoupon(couponId, userId);
        return couponConverter.couponDO2DTO(couponInfoDO);
    }

    /**
     * 创建优惠券
     * 简化创建过程
     *
     * @param createCouponReq 优惠券入参
     */
    @Override
    public void createCoupon(CreateCouponReq createCouponReq) {
        CouponInfoDO couponDO = couponConverter.couponReq2DO(createCouponReq);
        couponDAO.save(couponDO);
    }

    /**
     * 扣减优惠券
     *
     * @param couponDTO couponDTO
     */
    @Override
    public void deductionCoupon(CouponDTO couponDTO) {
        deductionCouponFactory.deduction(couponDTO);
    }

    /**
     * 模拟创建优惠券
     *
     * @param couponInfoDOList 模拟优惠券数据list
     */
    @Override
    public void mockCreateCoupon(List<CouponInfoDO> couponInfoDOList) {
        couponDAO.saveBatch(couponInfoDOList);
    }

}
