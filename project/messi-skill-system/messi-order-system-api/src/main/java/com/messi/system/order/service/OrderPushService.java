package com.messi.system.order.service;

import com.messi.system.order.domain.dto.OrderDetailDTO;

/**
 * 推送订单service
 */
public interface OrderPushService {

    /**
     * 推送订单到es
     */
    void sendOrderToEs(OrderDetailDTO orderDetailDTO);

}
