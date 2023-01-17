package com.messi.system.user.service;

/**
 * 用户service
 */
public interface UserService {

    /**
     * 检查用户合法性
     *
     * @param userId 用户id
     */
    Boolean checkUserValidity(String userId);
}
