package com.messi.system.rocketmq.message;

import org.apache.rocketmq.common.message.Message;

/**
 * 自定义mq消息格式
 */
public class MqMessage extends Message {

    private static final long serialVersionUID = 3149965835794203052L;

    public MqMessage(String topic, byte[] body) {
        super(topic, body);
    }
}
