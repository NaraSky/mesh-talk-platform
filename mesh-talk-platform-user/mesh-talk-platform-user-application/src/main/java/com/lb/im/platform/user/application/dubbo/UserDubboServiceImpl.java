package com.lb.im.platform.user.application.dubbo;

import com.lb.im.platform.common.model.constants.IMPlatformConstants;
import com.lb.im.platform.common.model.entity.User;
import com.lb.im.platform.dubbo.user.UserDubboService;
import com.lb.im.platform.user.application.service.UserService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@DubboService(version = IMPlatformConstants.DEFAULT_DUBBO_VERSION)
public class UserDubboServiceImpl implements UserDubboService {

    @Autowired
    private UserService userService;

    @Override
    public User getUserById(Long id) {
        return userService.getUserById(id);
    }
}
