package com.lb.im.platform.user.domain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lb.im.platform.common.model.entity.User;

import java.util.List;

public interface UserDomainService extends IService<User> {

    /**
     * 根据用户名获取用户信息
     */
    User getUserByUserName(String userName);

    /**
     * 保存用户
     */
    void saveOrUpdateUser(User user);

    /**
     * 根据名称模糊查询用户列表
     */
    List<User> getUserListByName(String name);

}
