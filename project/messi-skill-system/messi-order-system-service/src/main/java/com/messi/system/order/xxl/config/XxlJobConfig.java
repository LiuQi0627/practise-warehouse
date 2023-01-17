package com.messi.system.order.xxl.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * xxl-job配置
 */
@Slf4j
@Configuration
public class XxlJobConfig {

    @Value("${xxl.job.admin.address}")
    private String address;

    @Value("${xxl.job.executor.name}")
    private String name;

    @Bean
    public XxlJobSpringExecutor xxlJobSpringExecutor() {
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(address);
        xxlJobSpringExecutor.setAppname(name);

        return xxlJobSpringExecutor;
    }
}
