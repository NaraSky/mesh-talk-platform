package com.lb.im.platform.dubbo.user;

import com.lb.im.platform.common.model.entity.User;

public interface UserDubboService {

    User getUserById(Long id);
}
