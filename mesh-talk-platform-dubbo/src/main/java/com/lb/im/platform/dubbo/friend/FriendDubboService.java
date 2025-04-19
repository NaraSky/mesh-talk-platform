package com.lb.im.platform.dubbo.friend;

import java.util.List;

/**
 * 好友模块Dubbo服务接口
 * 该接口定义了好友关系相关的RPC服务方法，供其他微服务调用
 * 技术：使用Dubbo作为RPC框架实现微服务间的通信
 */
public interface FriendDubboService {

    /**
     * 判断两个用户是否为好友关系
     * 
     * @param userId1 用户ID1
     * @param userId2 用户ID2
     * @return 如果是好友返回true，否则返回false
     */
    Boolean isFriend(Long userId1, Long userId2);

    /**
     * 获取指定用户的所有好友ID列表
     * 
     * @param userId 用户ID
     * @return 好友ID列表
     */
    List<Long> getFriendIdList(Long userId);
}
