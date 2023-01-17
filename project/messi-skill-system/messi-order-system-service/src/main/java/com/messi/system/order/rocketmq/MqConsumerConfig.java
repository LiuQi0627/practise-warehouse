package com.messi.system.order.rocketmq;

import com.messi.system.constant.RocketMqConstants;
import com.messi.system.order.rocketmq.listener.NotPaidCancelOrderListener;
import com.messi.system.order.service.OrderService;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * rocketmq consumer配置
 */
@Configuration
public class MqConsumerConfig {

    @Value("${rocketmq.name-server}")
    private String nameServer;

    /**
     * 普通的rocketmq consumer config
     */
    @Configuration
    public static class DefaultRocketMqConsumerConfig {
        @Bean
        public NotPaidCancelOrderListener notPaidCancelOrderListener(OrderService orderService) {
            return new NotPaidCancelOrderListener(orderService);
        }
    }

    /**
     * 支付前取消订单消息的消费者
     *
     * @param notPaidCancelOrderListener 支付前取消订单的消息监听器
     * @return DefaultMQPushConsumer
     */
    @Bean("notPaidCancelOrderConsumer")
    public DefaultMQPushConsumer notPaidCancelOrderConsumer(NotPaidCancelOrderListener notPaidCancelOrderListener) {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(RocketMqConstants.NOT_PAID_CANCEL_ORDER_GROUP);

        try {
            consumer.setNamesrvAddr(nameServer);
            consumer.subscribe(RocketMqConstants.NOT_PAID_CANCEL_ORDER_TOPIC, "*");
            consumer.registerMessageListener(notPaidCancelOrderListener);
            consumer.start();

        } catch (MQClientException e) {
            e.printStackTrace();
        }

        return consumer;
    }
}
