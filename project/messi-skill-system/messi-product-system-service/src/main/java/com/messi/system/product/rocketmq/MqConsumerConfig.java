package com.messi.system.product.rocketmq;

import com.messi.system.constant.RocketMqConstants;
import com.messi.system.product.rocketmq.listener.DeductionStockListener;
import com.messi.system.product.service.ProductService;
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
        public DeductionStockListener deductionCouponListener(ProductService productService) {
            return new DeductionStockListener(productService);
        }
    }

    /**
     * 支付前取消订单扣减库存的事务消息消费者
     *
     * @param deductionStockListener 提交订单扣减库存的消息监听器
     * @return DefaultMQPushConsumer
     */
    @Bean("deductionStockTransactionConsumer")
    public DefaultMQPushConsumer deductionStockTransactionConsumer(DeductionStockListener deductionStockListener) {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(RocketMqConstants.DEDUCTION_STOCK_TRANSACTION_GROUP);

        try {
            consumer.setNamesrvAddr(nameServer);
            consumer.subscribe(RocketMqConstants.NOT_PAID_CANCEL_ORDER_DEDUCTION_TRANSACTION_TOPIC, "*");
            consumer.registerMessageListener(deductionStockListener);
            consumer.start();

        } catch (MQClientException e) {
            e.printStackTrace();
        }

        return consumer;
    }
}
