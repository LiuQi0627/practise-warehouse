package com.messi.system.order.rocketmq.listener;

import com.alibaba.fastjson.JSONObject;
import com.messi.system.order.converter.OrderConverter;
import com.messi.system.order.domain.request.CancelOrderReq;
import com.messi.system.order.rocketmq.msg.NotPaidCancelOrderMsgReq;
import com.messi.system.order.service.OrderService;
import com.messi.system.rocketmq.listener.AbstractMessageListenerConcurrently;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 支付前取消订单的消息监听器
 */
@Slf4j
@Component
public class NotPaidCancelOrderListener extends AbstractMessageListenerConcurrently {

    private final OrderService orderService;

    public NotPaidCancelOrderListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @Autowired
    private OrderConverter orderConverter;

    @Override
    public ConsumeConcurrentlyStatus onMessage(String message) {
        NotPaidCancelOrderMsgReq msgReq = JSONObject.parseObject(message, NotPaidCancelOrderMsgReq.class);
        log.info("取得消息参数：{}", msgReq);

        CancelOrderReq cancelOrderReq = orderConverter.msgReq2CancelOrderReq(msgReq);
        orderService.notPaidCancelOrder(cancelOrderReq);

        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}
