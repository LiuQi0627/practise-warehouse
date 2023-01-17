package com.messi.system.user.service.impl;

import com.messi.system.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户service实现类
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Override
    public Boolean checkUserValidity(String userId) {
        log.info("用户身份核验通过,用户id:{}", userId);
        return true;
    }
}
