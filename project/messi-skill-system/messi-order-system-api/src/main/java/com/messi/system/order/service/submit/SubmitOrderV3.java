package com.messi.system.order.service.submit;

import com.messi.system.order.domain.builder.Order;
import com.messi.system.order.domain.dto.CheckCouponDTO;
import com.messi.system.order.domain.request.SubmitOrderReq;

import java.util.List;

public interface SubmitOrderV3 extends SubmitOrder {

    void frameworkDeductionCoupon(CheckCouponDTO checkCouponDTO);

    void frameworkDeductionStock(List<SubmitOrderReq.OrderProduct> orderProductList);

    void saveOrder(Order order);

    void aloneDeductionStock(SubmitOrderReq.OrderProduct orderProduct);

    void deductionCoupon(CheckCouponDTO checkCouponDTO);
}
