package com.messi.system.core;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 封装统一的响应结果对象
 */
@Getter
@Setter
public class ResResult<T> implements Serializable {

    private static final long serialVersionUID = 4478321762933111235L;

    /**
     * 业务数据
     */
    private T data;

    /**
     * 响应码
     */
    private String resCode;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 请求执行
     */
    private Boolean resSuccess;

    /**
     * 默认失败错误码
     */
    private static final String DEFAULT_ERROR_CODE = "-1";

    /**
     * 默认成功响应码
     */
    private static final String DEFAULT_SUCCESS_CODE = "200";

    /**
     * 请求执行成功
     */
    private static final Boolean REQ_EXEC_SUCCESS = true;

    /**
     * 请求执行失败
     */
    private static final Boolean REQ_EXEC_FAILED = false;

    public ResResult() {
    }

    public ResResult(Boolean resSuccess, T data, String resCode, String errorMsg) {
        this.resSuccess = resSuccess;
        this.data = data;
        this.resCode = resCode;
        this.errorMsg = errorMsg;
    }

    /**
     * 请求执行成功，不返回数据
     */
    public static <T> ResResult<T> buildSuccess() {
        return new ResResult<>(REQ_EXEC_SUCCESS, null, DEFAULT_SUCCESS_CODE, null);
    }

    /**
     * 请求执行成功，返回数据
     */
    public static <T> ResResult<T> buildSuccess(T data) {
        return new ResResult<>(REQ_EXEC_SUCCESS, data, DEFAULT_SUCCESS_CODE, null);
    }

    /**
     * 请求执行失败，返回固定错误码
     */
    public static <T> ResResult<T> buildError(String errorMsg) {
        return new ResResult<>(REQ_EXEC_FAILED, null, DEFAULT_ERROR_CODE, errorMsg);
    }

    /**
     * 请求执行失败，返回自定义异常码和错误信息
     */
    public static <T> ResResult<T> buildError(String errCode, String errMsg) {
        return new ResResult<>(REQ_EXEC_FAILED, null, errCode, errMsg);
    }


}
