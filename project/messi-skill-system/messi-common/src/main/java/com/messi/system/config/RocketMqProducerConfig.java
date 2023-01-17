package com.messi.system.config;

import com.messi.system.rocketmq.producer.MqProducer;
import com.messi.system.rocketmq.producer.MqProducerImpl;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * rocketmq配置producer
 */
@Configuration
@SuppressWarnings(value = {"all"})
public class RocketMqProducerConfig {

    /**
     * 配置加载 default producer
     *
     * @param rocketMQProperties rocketmq配置
     * @return
     */
    @Bean(initMethod = "init", destroyMethod = "shutdown")
    public MqProducer defaultProducerImpl(RocketMQProperties rocketMQProperties) {
        return new MqProducerImpl(rocketMQProperties);
    }

}
