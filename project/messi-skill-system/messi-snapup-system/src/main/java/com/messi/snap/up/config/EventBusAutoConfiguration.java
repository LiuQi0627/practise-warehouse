package com.messi.snap.up.config;

import com.messi.snap.up.annotation.Channel;
import com.messi.snap.up.config.bus.BossConfig;
import com.messi.snap.up.config.bus.ThreadPoolConfig;
import com.messi.snap.up.config.bus.WorkerConfig;
import com.messi.snap.up.config.conditional.EventBusCondition;
import com.messi.snap.up.config.conditional.WorkerThreadExecutorCondition;
import com.messi.snap.up.reactor.bus.BossEventBus;
import com.messi.snap.up.reactor.bus.WorkEventBus;
import com.messi.snap.up.reactor.executor.WorkerThreadExecutor;
import com.messi.snap.up.reactor.listener.EventListener;
import com.messi.snap.up.reactor.manager.WorkEventBusManager;
import com.messi.snap.up.utils.CglibUtils;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Map;

@Configuration
@EnableConfigurationProperties({BossConfig.class, WorkerConfig.class, ThreadPoolConfig.class})
public class EventBusAutoConfiguration implements ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware {

    private final BossConfig bossConfig;

    private final WorkerConfig workerConfig;

    private final ThreadPoolConfig threadPoolConfig;

    private ApplicationContext applicationContext;

    public EventBusAutoConfiguration(BossConfig bossConfig, WorkerConfig workerConfig, ThreadPoolConfig threadPoolConfig) {
        this.bossConfig = bossConfig;
        this.workerConfig = workerConfig;
        this.threadPoolConfig = threadPoolConfig;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * BossEvent+WorkerEvent双总线架构，基于内存队列做异步任务的转发处理
     * 系统启动后自定义一个回调，把listeners注册到指定的worker event bus
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Map<String, EventListener> eventListenerMap = applicationContext.getBeansOfType(EventListener.class);
        WorkEventBusManager workEventBusManager = WorkEventBusManager.getSingleton();

        for (EventListener eventListener : eventListenerMap.values()) {
            Class<?> realClazz = CglibUtils.filterCglibProxyClass(eventListener.getClass());
            //  识别自定义的channel注解
            Channel channel = realClazz.getAnnotation(Channel.class);
            if (channel != null && !channel.value().isEmpty()) {
                //  获取work event bus,注册到监听器
                workEventBusManager.getWorkEventBus(channel.value()).register(eventListener);
            }
        }
    }

    /**
     * 匹配EventBusCondition类，自定义BossEventBus组件
     */
    @Bean
    @Conditional(EventBusCondition.class)
    @ConditionalOnMissingBean
    public BossEventBus bossEventBus() {
        return new BossEventBus(bossConfig, workerConfig);
    }

    @Bean
    @Conditional(WorkerThreadExecutorCondition.class)
    @ConditionalOnMissingBean
    public WorkerThreadExecutor executorService() {
        return new WorkerThreadExecutor(threadPoolConfig);
    }
}
