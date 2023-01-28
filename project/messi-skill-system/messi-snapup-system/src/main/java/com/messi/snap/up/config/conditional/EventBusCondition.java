package com.messi.snap.up.config.conditional;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 事件总线配置匹配类
 */
public class EventBusCondition implements Condition {

    /**
     * 系统启动时,默认匹配创建boss event bus 和 第一个worker event bus
     */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return context.getEnvironment().containsProperty("messi.snap.up.event.bus.boss.ringBufferSize")
                && context.getEnvironment().containsProperty("messi.snap.up.event.bus.boss.eventHandlerNum")
                && context.getEnvironment().containsProperty("messi.snap.up.event.bus.workers[0].channel")
                && context.getEnvironment().containsProperty("messi.snap.up.event.bus.workers[0].ringBufferSize")
                && context.getEnvironment().containsProperty("messi.snap.up.event.bus.workers[0].eventHandlerNum");
    }

}
