package com.messi.system.consistency.instance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 封装一致性任务的实例信息
 */
@Data
@Builder
@AllArgsConstructor
public class ConsistencyTaskInstance implements Serializable {
    private static final long serialVersionUID = -6528985577952322784L;

    /**
     * 本次执行最终一致性任务的方法，格式：全限定名称
     * 如果一个方法被多次调用，task是相同的
     */
    private String task;

    /**
     * 执行的任务方法名称
     */
    private String taskMethodName;

    /**
     * 完整的方法签名：
     * 类路径#方法名(参数1的类型,参数2的类型,...参数N的类型)
     */
    private String fullSignName;

    /**
     * 执行的任务方法的参数类型
     */
    private String paramTypes;

    /**
     * 执行参数,JSON格式保存
     */
    private String params;

    /**
     * 任务执行类型，默认立即执行
     */
    private Integer execType;

    /**
     * 任务执行间隔，单位：ms
     */
    private Integer execInterval;

    /**
     * 任务延迟执行时间,单位:ms
     */
    private Integer delayMs;

    /**
     * 任务执行次数
     */
    private Integer execTotal;

    /**
     * 任务首次执行时间
     */
    private Date execTime;

    /**
     * 任务状态
     */
    private Integer status;

    /**
     * 任务执行失败后的异常信息
     */
    private String errMsg;

    /**
     * 任务执行失败后的降级执行类
     */
    private String downgradeClass;

    /**
     * 降级执行类执行失败的异常信息
     */
    private String downgradeErrorMsg;

    /**
     * 主键id
     */
    private Long id;

    private Date createTime;

    private Date modifiedTime;

    public ConsistencyTaskInstance() {

    }

}
