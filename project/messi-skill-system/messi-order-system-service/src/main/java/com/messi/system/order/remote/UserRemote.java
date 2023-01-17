package com.messi.system.order.remote;

import com.messi.system.user.api.UserServiceApi;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/**
 * 订单系统封装的 调用用户服务远程接口 的组件
 */
@Component
public class UserRemote {

    @DubboReference(retries = 0)
    private UserServiceApi userServiceApi;

    /**
     * 检查用户合法性
     *
     * @param userId 用户id
     */
    public Boolean checkUserValidity(String userId) {
        return userServiceApi.checkUserValidity(userId);
    }
}
