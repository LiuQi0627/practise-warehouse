package com.messi.system.order.remote;

import com.messi.system.market.api.CouponServiceApi;
import com.messi.system.market.domain.dto.CouponDTO;
import com.messi.system.order.converter.OrderConverter;
import com.messi.system.order.domain.dto.CheckCouponDTO;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 订单系统封装的 调用营销服务优惠券远程接口 的组件
 */
@Component
public class CouponRemote {

    @DubboReference(retries = 0)
    public CouponServiceApi couponServiceApi;

    @Autowired
    private OrderConverter orderConverter;

    /**
     * 获取优惠券
     */
    public CheckCouponDTO getCoupon(String couponId, String userId) {
        CouponDTO couponDTO = couponServiceApi.getCoupon(couponId, userId);
        return orderConverter.couponDTO2CheckCouponDTO(couponDTO);
    }

    /**
     * 计算满减金额
     *
     * @param checkCouponDTO checkCouponDTO
     * @return 满减金额
     */
    public Integer getCouponDeductionPrice(CheckCouponDTO checkCouponDTO) {
        CouponDTO couponDTO = orderConverter.checkCouponDTO2CouponDTO(checkCouponDTO);
        return couponServiceApi.getCouponDeductionPrice(couponDTO);
    }

    public void deductionCoupon(CheckCouponDTO checkCouponDTO) {
        CouponDTO couponDTO = orderConverter.checkCouponDTO2CouponDTO(checkCouponDTO);
        couponServiceApi.deductionCoupon(couponDTO);
    }
}
