package com.messi.system.order.thread;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 自定义固定线程池配置
 */
@Configuration
public class CustomThreadPoolConfig {

    private static final String THREAD_NAME_PREFIX = "Push-OrderData-To-Elasticsearch ==== > ";

    /**
     * 自定义推送es数据固定线程池
     */
    @Bean("pushEsDataFixedThreadPool")
    public ThreadPoolTaskExecutor threadPoolExecutor() {
        //  自定义1个核心线程的spring封装的固定线程池
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(1);
        threadPoolTaskExecutor.setMaxPoolSize(1);
        threadPoolTaskExecutor.setQueueCapacity(10000);
        threadPoolTaskExecutor.setKeepAliveSeconds(60);
        threadPoolTaskExecutor.setThreadNamePrefix(THREAD_NAME_PREFIX);

        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
}
