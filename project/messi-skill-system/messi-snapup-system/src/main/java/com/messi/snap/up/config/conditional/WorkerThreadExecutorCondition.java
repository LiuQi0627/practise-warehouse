package com.messi.snap.up.config.conditional;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 具体执行worker bus里的事件的自定义线程执行器类配置匹配类
 */
public class WorkerThreadExecutorCondition implements Condition {

    /**
     * 配合worker[0],这里配置对接worker[0]的线程执行器
     */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return context.getEnvironment().containsProperty("messi.snap.up.threads[0].threadPool")
                && context.getEnvironment().containsProperty("messi.snap.up.threads[0].threadCount");
    }
}
