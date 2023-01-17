package com.messi.system.user.api.impl;

import com.messi.system.user.api.UserServiceApi;
import com.messi.system.user.service.UserService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 用户 dubbo api 实现类
 */
@DubboService
public class UserServiceApiImpl implements UserServiceApi {

    @Autowired
    private UserService userService;

    @Override
    public Boolean checkUserValidity(String userId) {
        return userService.checkUserValidity(userId);
    }
}
