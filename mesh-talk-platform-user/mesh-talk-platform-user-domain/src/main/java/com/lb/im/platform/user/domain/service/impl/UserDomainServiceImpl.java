package com.lb.im.platform.user.domain.service.impl;

import cn.hutool.core.collection.CollectionUtil;
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

import java.util.Collections;
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
        boolean result = this.saveOrUpdate(user);
        //更新成功
        if (result){
            //TODO 发布更新缓存事件
        }
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

    @Override
    public User getById(Long userId) {
        return super.getById(userId);
    }

    @Override
    public List<User> findUserByName(String name) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.like(User::getUserName, name).or().like(User::getNickName, name).last("limit 20");
        List<User> list = this.list(queryWrapper);
        if (CollectionUtil.isEmpty(list)){
            return Collections.emptyList();
        }
        return list;
    }
}
