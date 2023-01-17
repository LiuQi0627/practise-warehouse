package com.messi.system.order.service.submit;

import com.messi.system.order.domain.dto.CheckCouponDTO;
import com.messi.system.order.domain.request.SubmitOrderReq;

import java.util.List;

public interface SubmitOrderV2 extends SubmitOrder {

    /**
     * 提交分布式事务消息做扣减操作
     *
     * @param checkCouponDTO   优惠券信息
     * @param orderProductList 商品信息
     */
    void sendTransactionMsgDeduction(CheckCouponDTO checkCouponDTO, List<SubmitOrderReq.OrderProduct> orderProductList);

}
