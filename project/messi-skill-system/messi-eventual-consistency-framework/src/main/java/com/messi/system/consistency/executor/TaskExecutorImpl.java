package com.messi.system.consistency.executor;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.messi.system.consistency.instance.ConsistencyTaskInstance;
import com.messi.system.consistency.service.TaskInstanceService;
import com.messi.system.consistency.util.ThreadLocalFlagUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

/**
 * 任务执行器
 */
@Slf4j
@Service
public class TaskExecutorImpl implements TaskExecutor {

    @Autowired
    private TaskInstanceService taskInstanceService;

    @Autowired
    private BeanFactory beanFactory;

    private static final String PREFIX = "{";

    @Override
    public ConsistencyTaskInstance execTaskInstance(ConsistencyTaskInstance consistencyTaskInstance) {

        try {
            //  1、配置任务启动项
            consistencyTaskInstance = taskInstanceService.configureStartupItems(consistencyTaskInstance);

            //  2、把任务更新到本地消息
            taskInstanceService.updateTaskInstance(consistencyTaskInstance);

            //  3、执行任务
            this.exec(consistencyTaskInstance);

            //  4、删除本地消息表记录
            taskInstanceService.removeTaskInstance(consistencyTaskInstance);

            //  5、返回储存在内存中的实例
            log.info("最终一致性任务实例执行成功:{}", consistencyTaskInstance.getFullSignName());
            return consistencyTaskInstance;

        } catch (Exception e) {
            //  更新任务执行失败标记
            taskInstanceService.updateFailedTaskInstance(consistencyTaskInstance);

            log.error("最终一致性任务实例执行失败:{}", consistencyTaskInstance.getFullSignName());
            return consistencyTaskInstance;
        }

    }

    /**
     * 指定执行的任务实例
     */
    private void exec(ConsistencyTaskInstance consistencyTaskInstance) {
        try {
            //  获取全限定类型
            String fullSignName = consistencyTaskInstance.getFullSignName();

            //  获取方法的类
            Class<?> clazz = Class.forName(fullSignName.split("#")[0]);

            if (ObjectUtil.isEmpty(clazz)) {
                log.error("class获取失败:{}", fullSignName);
                return;
            }

            //  获取spring容器里的bean
            Object bean = beanFactory.getBean(clazz);
            if (ObjectUtil.isEmpty(bean)) {
                log.error("spring bean获取失败：{}", fullSignName);
                return;
            }

            //  获取要执行的目标方法
            //  方法名
            String taskMethodName = consistencyTaskInstance.getTask();
            Class<?>[] methodTypes = getMethodTypes(consistencyTaskInstance);
            //  组装成目标方法
            Method targetMethod = clazz.getMethod(taskMethodName, methodTypes);
            if (ObjectUtil.isEmpty(targetMethod)) {
                log.error("组装成目标方法失败：{}", fullSignName);
                return;
            }

            //  做构造方法参数入参
            String params = consistencyTaskInstance.getParams();
            Object[] args = methodInputParam(params, methodTypes);

            //  设置本地线程执行任务标记
            ThreadLocalFlagUtil.markExecuted();

            //  通过反射执行
            //  这里是真正触发业务方法执行的位置
            targetMethod.invoke(bean, args);

            //  执行后，立即标记当前任务未执行
            ThreadLocalFlagUtil.markNotExecuted();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("TaskExecutorImpl.exec执行失败。");
        }

    }

    /**
     * 构造方法入参
     *
     * @param paramsJson  JSON类型的参数值
     * @param methodTypes 参数类型
     */
    private Object[] methodInputParam(String paramsJson, Class<?>[] methodTypes) {
        JSONArray paramArray = JSONObject.parseArray(paramsJson);
        Object[] args = new Object[paramArray.size()];

        for (int i = paramArray.size() - 1; i >= 0; i--) {
            if (paramArray.getString(i).startsWith(PREFIX)) {
                //  针对参数的对象是实体
                args[i] = JSONUtil.toBean(paramArray.getString(i), methodTypes[i]);
            } else {
                //  非实体类型
                args[i] = paramArray.get(i);
            }
        }
        return args;
    }

    /**
     * 拼接方法参数类型
     *
     * @return 参数类型的class数组
     */
    private static Class<?>[] getMethodTypes(ConsistencyTaskInstance consistencyTaskInstance) {
        //  参数类型数组
        String[] strParamTypes = consistencyTaskInstance.getParamTypes().split(",");

        Class<?>[] paramTypes = new Class<?>[strParamTypes.length];
        for (int i = strParamTypes.length - 1; i >= 0; i--) {
            try {
                paramTypes[i] = Class.forName(strParamTypes[i]);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return paramTypes;
    }
}
