package com.messi.system.consistency.annotation.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 最终一致性框架参数配置
 */
@Data
@Builder
@AllArgsConstructor
//@ConfigurationProperties(prefix = "messi.consistency.framework.param.pool")
public class ConsistencyFrameworkParamProperties {

}

