package com.messi.system.rocketmq.producer;

import com.messi.system.constant.RocketMqConstants;
import com.messi.system.rocketmq.message.MqMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;

import java.nio.charset.StandardCharsets;

/**
 * rocketmq 发送消息实现
 */
@Slf4j
public class MqProducerImpl implements MqProducer {

    //  发送 普通消息 / 延迟消息
    private final DefaultMQProducer defaultMQProducer;

    //  发送事务消息
    private final TransactionMQProducer transactionMqProducer;

    public MqProducerImpl(RocketMQProperties rocketMQProperties) {
        this.defaultMQProducer = new DefaultMQProducer(RocketMqConstants.DEFAULT_PRODUCER_GROUP);
        this.defaultMQProducer.setNamesrvAddr(rocketMQProperties.getNameServer());

        this.transactionMqProducer = new TransactionMQProducer(RocketMqConstants.DEFAULT_TRANSACTION_PRODUCER_GROUP);
        this.transactionMqProducer.setNamesrvAddr(rocketMQProperties.getNameServer());
    }

    /**
     * 执行前，做初始化
     */
    public void init() {
        try {
            this.defaultMQProducer.start();
            this.transactionMqProducer.start();
        } catch (MQClientException e) {
            log.error("defaultMQProducer start failed", e);
        }
    }

    /**
     * 执行后，做关闭
     */
    public void shutdown() {
        this.defaultMQProducer.shutdown();
        this.transactionMqProducer.shutdown();
    }

    @Override
    public void sendMessage(String topic, String message, Integer delayTimeLevel, String type) {
        //  自定义消息体
        MqMessage mqMessage = new MqMessage(topic, message.getBytes(StandardCharsets.UTF_8));

        //  设置消息延迟等级
        if (delayTimeLevel > 0) {
            mqMessage.setDelayTimeLevel(delayTimeLevel);
        }

        //  发送消息
        try {
            SendResult sendResult = defaultMQProducer.send(mqMessage);
            if (SendStatus.SEND_OK != sendResult.getSendStatus()) {
                throw new RuntimeException("MQ消息发送失败" + sendResult.getSendStatus().toString());
            }

            log.info("发送MQ消息成功,message:{}", message);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("MQ消息发送失败");
        }
    }

    @Override
    public TransactionMQProducer getTransactionProducer() {
        return transactionMqProducer;
    }
}
