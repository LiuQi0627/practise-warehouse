package com.messi.system.rocketmq.producer;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.TransactionMQProducer;

/**
 * rocketmq producer 模板
 * jdk8 default方法，在接口中做默认的方法实现
 */
public interface MqProducer {

    /**
     * 发送普通消息
     */
    default void sendMessage(String topic, String message) {
        sendMessage(topic, message, -1, "");
    }

    /**
     * 发送延迟消息
     */
    default void sendMessage(String topic, String message, Integer delayTimeLevel) {
        sendMessage(topic, message, delayTimeLevel, "");
    }

    /**
     * 发送消息模板
     *
     * @param topic          topic
     * @param message        消息体
     * @param delayTimeLevel 延迟消息等级
     * @param type           type
     */
    void sendMessage(String topic, String message, Integer delayTimeLevel, String type);

    /**
     * 获取事务消息producer
     */
    TransactionMQProducer getTransactionProducer();
}
