package com.lb.im.platform.user.domain.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lb.im.platform.common.exception.IMException;
import com.lb.im.platform.common.model.entity.User;
import com.lb.im.platform.common.model.enums.HttpCode;
import com.lb.im.platform.user.domain.repository.UserRepository;
import com.lb.im.platform.user.domain.service.UserDomainService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class UserDomainServiceImpl extends ServiceImpl<UserRepository, User> implements UserDomainService {

    @Override
    public User getUserByUserName(String userName) {
        if (StrUtil.isEmpty(userName)) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(User::getUserName, userName);
        return this.getOne(queryWrapper);
    }

    @Override
    public void saveOrUpdateUser(User user) {
        if (user == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        this.saveOrUpdate(user);
    }

    @Override
    public List<User> getUserListByName(String name) {
        if (StrUtil.isEmpty(name)) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.like(User::getUserName, name).or().like(User::getNickName, name).last("limit 20");
        return this.list(queryWrapper);
    }
}
