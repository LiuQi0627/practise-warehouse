package com.messi.system.consistency.util;

import cn.hutool.core.util.ObjectUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.StringJoiner;

/**
 * 反射工具
 */
public class ReflectUtil {

    private static final HashMap<String, Class<?>> PRIMITIVE_TYPE = new HashMap<String, Class<?>>() {
        private static final long serialVersionUID = -5763305277165512542L;

        {
            put("java.lang.Byte", byte.class);
            put("java.lang.Short", short.class);
            put("java.lang.Int", int.class);
            put("java.lang.Long", long.class);
            put("java.lang.Float", float.class);
            put("java.lang.Double", double.class);
            put("java.lang.Boolean", boolean.class);
            put("java.lang.Char", char.class);
        }
    };

    /**
     * 获取参数类型
     *
     * @return 切入点参数的类型
     */
    public static Class<?>[] getArgsClass(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Class<?>[] clz = new Class[args.length];

        for (int i = 0; i < args.length; i++) {
            //  如果参数是不是原始类型
            if (args[i] != null && !args[i].getClass().isPrimitive()) {
                //  获取其原始类型对应的封装类型
                String entityName = args[i].getClass().getName();
                Class<?> type = PRIMITIVE_TYPE.get(entityName);
                clz[i] = ObjectUtil.isEmpty(type) ? args[i].getClass() : type;
            }
        }

        return clz;
    }

    /**
     * 获取切入方法的全限定类名
     * 在需要添加大量标点符号时使用StringJoiner，比StringBuilder在拼接字符串上更加优雅
     *
     * @param joinPoint 切入点
     * @param argsClz   切入点参数的类型
     * @return 类路径#方法名(参数1类型, 参数2类型, ... 参数N类型)
     */
    public static String getMethodFullQualifiedName(JoinPoint joinPoint, Class<?>[] argsClz) {
        StringJoiner joiner = new StringJoiner("", "", "");

        //  拼接格式： 执行类#执行的方法
        joiner.add(joinPoint.getTarget().getClass().getName())
                .add("#")
                .add(joinPoint.getSignature().getName());

        //  前缀
        joiner.add("(");

        //  方法参数类型名称
        for (int i = 0; i < argsClz.length; i++) {
            String clzName = argsClz[i].getName();
            joiner.add(clzName);

            if (argsClz.length != (i + 1)) {   //  不是最后一个
                joiner.add(",");
            }
        }

        //  后缀
        joiner.add(")");

        //  举例格式格式
        //  com.messi.system.order.service.impl.submit.SubmitOrderServiceV3Impl#getOrder(java.lang.String,java.lang.Integer)
        return joiner.toString();
    }

    /**
     * 字符化执行方法参数类型
     *
     * @param argsTypes 方法参数类类型
     * @return 字符化的方法参数类型
     */
    public static String getStrArgsTypes(Class<?>[] argsTypes) {
        StringBuilder paramTypeStr = new StringBuilder();

        for (int i = 0; i < argsTypes.length; i++) {
            paramTypeStr.append(argsTypes[i].getName());

            if (argsTypes.length != (i + 1)) {
                paramTypeStr.append(",");
            }
        }

        return paramTypeStr.toString();

    }

}
