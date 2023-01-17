package com.messi.system.user.api;

/**
 * user system对外暴露的dubbo api接口
 */
public interface UserServiceApi {

    /**
     * 检查用户合法性
     *
     * @param userId 用户id
     */
    Boolean checkUserValidity(String userId);
}
