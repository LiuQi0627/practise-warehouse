package com.messi.system.consistency.aspect;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.messi.system.consistency.annotation.ConsistencyFramework;
import com.messi.system.consistency.enums.TaskExecStatusEnums;
import com.messi.system.consistency.enums.TaskExecTypeEnums;
import com.messi.system.consistency.instance.ConsistencyTaskInstance;
import com.messi.system.consistency.service.TaskInstanceService;
import com.messi.system.consistency.util.ReflectUtil;
import com.messi.system.consistency.util.ThreadLocalFlagUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Date;

/**
 * ConsistencyFramework注解的切面实现
 */
@Aspect
@Slf4j
@Component
public class AnnotationAspect {

    @Autowired
    private TaskInstanceService taskInstanceService;

    /**
     * 默认的任务执行次数
     */
    private final static Integer execTotal = 0;

    /**
     * 做注解的AOP环绕通知
     */
    @Around("@annotation(consistencyFramework)")
    public Object aroundConsistencyFramework(ProceedingJoinPoint joinPoint, ConsistencyFramework consistencyFramework) throws Throwable {
        //  方法声明
        String declaringTypeName = joinPoint.getSignature().getDeclaringTypeName();
        //  方法签名
        String methodName = joinPoint.getSignature().getName();
        //  方法参数
        Object[] args = joinPoint.getArgs();

        log.info("最终一致性框架执行方法：{},执行类：{},执行方法参数：{}", methodName, declaringTypeName, args);

        //  1、判断当前任务在本地线程中是否重复执行
        if (TaskExecTypeEnums.TASK_EXECUTING.getCode().equals(ThreadLocalFlagUtil.getExecFlag())) {
            //  此任务正在执行中，放行即可
            return joinPoint.proceed();
        }

        //  2、取得最终一致性任务实例
        ConsistencyTaskInstance consistencyTaskInstance = getConsistencyTaskInstance(joinPoint, consistencyFramework);

        //  3、本地消息表保存任务实例
        taskInstanceService.saveTaskInstance(consistencyTaskInstance);

        //  4、返回null使流程执行结束
        //  不让外部的业务方法继续执行，执行的业务从这里开始，交由最终一致性任务实例来执行
        return null;
    }

    /**
     * 创建最终一致性任务的实例
     *
     * @param joinPoint            切入点
     * @param consistencyFramework 使用最终一致性框架注解的任务
     * @return 执行任务实例
     */
    private ConsistencyTaskInstance getConsistencyTaskInstance(ProceedingJoinPoint joinPoint, ConsistencyFramework consistencyFramework) {
        //  获取执行方法参数的参数类型
        Class<?>[] argsTypes = ReflectUtil.getArgsClass(joinPoint);
        log.info("argsTypes:{}", Arrays.toString(Arrays.stream(argsTypes).toArray()));

        //  获取执行方法的全限定名称
        String fullQualifiedName = ReflectUtil.getMethodFullQualifiedName(joinPoint, argsTypes);
        log.info("fullQualifiedName:{}", fullQualifiedName);

        //  获取执行方法的参数类型
        String paramTypes = ReflectUtil.getStrArgsTypes(argsTypes);
        log.info("paramTypes:{}", paramTypes);

        //  最终一致性任务实例化
        ConsistencyTaskInstance consistencyTaskInstance = buildInstance(fullQualifiedName, paramTypes, consistencyFramework, joinPoint);
        log.info("consistencyTaskInstance:{}", consistencyTaskInstance);

        return consistencyTaskInstance;
    }

    private ConsistencyTaskInstance buildInstance(String fullQualifiedName, String paramTypes,
                                                  ConsistencyFramework consistencyFramework, ProceedingJoinPoint joinPoint) {
        ConsistencyTaskInstance instance = ConsistencyTaskInstance.builder()
                //  同一个方法被多次最终一致性任务调用，那么执行的方法名称始终是同一个
                .task(StrUtil.isEmpty(consistencyFramework.taskName()) ? fullQualifiedName : consistencyFramework.taskName())
                .taskMethodName(joinPoint.getSignature().getDeclaringTypeName())
                .fullSignName(fullQualifiedName)
                .paramTypes(paramTypes)
                .params(JSONObject.toJSONString(joinPoint.getArgs()))
                .execType(consistencyFramework.execType().getCode() != null
                        ? consistencyFramework.execType().getCode()
                        : TaskExecTypeEnums.IMMEDIATELY_EXECUTE.getCode())
                .execInterval(consistencyFramework.execInterval())
                .delayMs(consistencyFramework.delayMs())
                .status(TaskExecStatusEnums.NEW.getCode())
                .execTotal(execTotal)
                .errMsg(null)
                .downgradeClass(consistencyFramework.downgradeMethod().getName())
                .downgradeErrorMsg(null)
                .createTime(new Date())
                .modifiedTime(new Date())
                .build();

        //  立即执行
        if (TaskExecTypeEnums.IMMEDIATELY_EXECUTE.getCode().equals(instance.getExecType())) {
            //  计算首次执行时间 = 当前时间
            instance.setExecTime(new Date());
        } else {
            //  计算首次执行时间 = 当前时间 + 任务指定的延期执行时间
            instance.setExecTime(new Date(System.currentTimeMillis() + instance.getDelayMs()));
        }

        return instance;
    }
}
