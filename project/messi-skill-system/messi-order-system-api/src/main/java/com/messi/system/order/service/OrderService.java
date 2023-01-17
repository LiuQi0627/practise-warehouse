package com.messi.system.order.service;

import com.messi.system.order.domain.entity.OrderInfoDO;
import com.messi.system.order.domain.request.CancelOrderReq;

/**
 * 订单service
 */
public interface OrderService {

    /**
     * 支付前取消订单的操作
     */
    void notPaidCancelOrder(CancelOrderReq cancelOrderReq);

    /**
     * 查询订单
     */
    OrderInfoDO getOrder(String orderId);

}
