package com.messi.system.market.converter;

import com.messi.system.market.domain.dto.CouponDTO;
import com.messi.system.market.domain.entity.CouponInfoDO;
import com.messi.system.market.domain.request.CreateCouponReq;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

/**
 * 营销中心优惠券对象转换器
 */
@Component
@Mapper(componentModel = "spring")
public interface CouponConverter {

    /**
     * CouponInfoDO 转换 CouponDTO
     */
    CouponDTO couponDO2DTO(CouponInfoDO couponInfoDO);

    /**
     * CouponDTO 转换 CouponInfoDO
     */
    CouponInfoDO couponDTO2DO(CouponDTO couponDTO);

    /**
     * CreateCouponReq 转换 CouponInfoDO
     */
    CouponInfoDO couponReq2DO(CreateCouponReq createCouponReq);

}
