package com.messi.system.consistency.annotation;

import com.messi.system.consistency.enums.TaskExecTypeEnums;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 提供给外部业务类使用的最终一致性任务框架注解
 */
@Retention(RetentionPolicy.RUNTIME) //  注解保留策略
@Target(ElementType.METHOD) //  注解修饰的对象范围
public @interface ConsistencyFramework {

    /**
     * 标注要执行的任务名称，指代具体执行最终一致性任务的方法
     */
    String taskName() default "";

    /**
     * 任务的执行间隔
     * 默认每10s执行一次,单位 ms
     */
    int execInterval() default 10000;

    /**
     * 任务执行类型，默认立即执行
     */
    TaskExecTypeEnums execType() default TaskExecTypeEnums.IMMEDIATELY_EXECUTE;

    /**
     * 首次延迟执行的时间，默认首次延迟执行时间是当前系统时间的60s以后
     */
    int delayMs() default 60000;

    /**
     * 指定执行失败的降级方法，默认为空
     */
    Class<?> downgradeMethod() default void.class;

}
