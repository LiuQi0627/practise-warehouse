package com.messi.system.config;

import com.messi.system.constant.RocketMqConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 自定义公共的配置项
 */
@Configuration
@Import(value = {RedisConfig.class, MybatisPlusConfig.class, RocketMqProducerConfig.class, RocketMqConstants.class})
public class CommonAutoConfiguration {

}
