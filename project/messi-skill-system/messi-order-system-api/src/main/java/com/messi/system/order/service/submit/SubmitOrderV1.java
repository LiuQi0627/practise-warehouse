package com.messi.system.order.service.submit;

import com.messi.system.order.domain.dto.CheckCouponDTO;
import com.messi.system.order.domain.request.SubmitOrderReq;

import java.util.List;

public interface SubmitOrderV1 extends SubmitOrder {

    /**
     * 提交订单 分布式事务 扣减库存
     *
     * @param orderProductList 订单商品列表
     */
    void distributedTransactionDeductionStock(List<SubmitOrderReq.OrderProduct> orderProductList);

    /**
     * 提交订单 分布式事务 扣减优惠券
     *
     * @param checkCouponDTO 订单优惠券
     */
    void distributedTransactionDeductionCoupon(CheckCouponDTO checkCouponDTO);

}
