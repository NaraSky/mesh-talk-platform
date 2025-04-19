package com.lb.im.platform.friend.application.dubbo;

import com.lb.im.platform.common.model.constants.IMPlatformConstants;
import com.lb.im.platform.common.model.entity.Friend;
import com.lb.im.platform.dubbo.friend.FriendDubboService;
import com.lb.im.platform.friend.application.service.FriendService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 好友Dubbo服务实现类
 * 实现了好友模块对外提供的RPC服务
 * 技术：
 * 1. 使用Dubbo作为RPC框架
 * 2. 基于DDD的应用层设计
 * 3. 作为应用服务的门面，对外提供远程调用能力
 */
@Component
@DubboService(version = IMPlatformConstants.DEFAULT_DUBBO_VERSION)
public class FriendDubboServiceImpl implements FriendDubboService {

    /**
     * 好友应用服务
     */
    @Autowired
    private FriendService friendService;

    /**
     * 判断两个用户是否为好友关系
     * 
     * @param userId1 用户ID1
     * @param userId2 用户ID2
     * @return 如果是好友返回true，否则返回false
     */
    @Override
    public Boolean isFriend(Long userId1, Long userId2) {
        return friendService.isFriend(userId1, userId2);
    }

    /**
     * 获取指定用户的所有好友ID列表
     * 
     * @param userId 用户ID
     * @return 好友ID列表
     */
    @Override
    public List<Long> getFriendIdList(Long userId) {
        return friendService.getFriendIdList(userId);
    }

    @Override
    public List<Friend> getFriendByUserId(Long userId) {
        return friendService.getFriendByUserId(userId);
    }
}
