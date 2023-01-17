package com.messi.system.market.rocketmq;

import com.messi.system.constant.RocketMqConstants;
import com.messi.system.market.rocketmq.listener.DeductionCouponListener;
import com.messi.system.market.service.CouponService;
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
        public DeductionCouponListener deductionCouponListener(CouponService couponService) {
            return new DeductionCouponListener(couponService);
        }
    }

    /**
     * 支付前取消订单扣减优惠券的事务消息消费者
     *
     * @param deductionCouponListener 提交订单扣减优惠券的消息监听器
     * @return DefaultMQPushConsumer
     */
    @Bean("deductionCouponTransactionConsumer")
    public DefaultMQPushConsumer deductionCouponTransactionConsumer(DeductionCouponListener deductionCouponListener) {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(RocketMqConstants.DEDUCTION_COUPON_TRANSACTION_GROUP);

        try {
            consumer.setNamesrvAddr(nameServer);
            consumer.subscribe(RocketMqConstants.NOT_PAID_CANCEL_ORDER_DEDUCTION_TRANSACTION_TOPIC, "*");
            consumer.registerMessageListener(deductionCouponListener);
            consumer.start();

        } catch (MQClientException e) {
            e.printStackTrace();
        }

        return consumer;
    }
}
