package com.messi.system.consistency.annotation;

import com.messi.system.consistency.annotation.selector.ConsistencyFrameworkSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 开启最终一致性框架的注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({ConsistencyFrameworkSelector.class})    //  导入配置选择器
public @interface EnableConsistencyFramework {

}
