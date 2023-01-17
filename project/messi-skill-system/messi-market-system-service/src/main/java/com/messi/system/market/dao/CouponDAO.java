package com.messi.system.market.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.messi.system.market.domain.entity.CouponInfoDO;
import com.messi.system.market.enums.MarketCouponEnums;
import com.messi.system.market.mapper.CouponInfoMapper;
import com.messi.system.mybatis.BaseDAO;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * 操作coupon DAO
 */
@Repository
public class CouponDAO extends BaseDAO<CouponInfoMapper, CouponInfoDO> {

    public CouponInfoDO getCoupon(String couponId, String userId) {
        LambdaQueryWrapper<CouponInfoDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CouponInfoDO::getCouponId, couponId);
        queryWrapper.eq(CouponInfoDO::getUserId, userId);

        return getOne(queryWrapper);
    }

    public void updateCouponUseStatus(String couponId, String userId) {
        LambdaUpdateWrapper<CouponInfoDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper
                .set(CouponInfoDO::getUseStatus, MarketCouponEnums.COUPON_IS_USED.getCode())
                .set(CouponInfoDO::getUsageTime, new Date())
                .set(CouponInfoDO::getModifiedTime, new Date())
                .eq(CouponInfoDO::getUserId, userId)
                .eq(CouponInfoDO::getCouponId, couponId);

        update(updateWrapper);
    }
}
