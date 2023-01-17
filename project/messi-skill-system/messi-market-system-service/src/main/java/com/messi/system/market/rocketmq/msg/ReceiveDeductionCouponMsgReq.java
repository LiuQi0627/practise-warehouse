package com.messi.system.market.rocketmq.msg;

import com.messi.system.market.domain.dto.CouponDTO;
import lombok.Data;

import java.io.Serializable;

/**
 * 接收扣减优惠券的消息
 */
@Data
public class ReceiveDeductionCouponMsgReq implements Serializable {
    private static final long serialVersionUID = 7846165419819883421L;

    /**
     * 优惠券信息
     */
    private CouponDTO couponDTO;
}
