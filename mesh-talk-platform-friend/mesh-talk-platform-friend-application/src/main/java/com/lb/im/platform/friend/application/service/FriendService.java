package com.lb.im.platform.friend.application.service;

import com.lb.im.platform.common.model.entity.Friend;
import com.lb.im.platform.common.model.vo.FriendVO;

import java.util.List;

/**
 * 好友应用服务接口
 * 定义了好友模块对外提供的服务方法
 * 技术：
 * 1. 基于DDD的应用层设计
 * 2. 作为领域服务的外观(Facade)，对外提供友好的API
 * 3. 负责协调领域对象完成业务逻辑
 */
public interface FriendService {

    /**
     * 获取用户的好友ID列表
     * 
     * @param userId 用户ID
     * @return 好友ID列表
     */
    List<Long> getFriendIdList(Long userId);

    /**
     * 判断两个用户是否为好友关系
     * 
     * @param userId1 用户ID1
     * @param userId2 用户ID2
     * @return 如果是好友返回true，否则返回false
     */
    Boolean isFriend(Long userId1, Long userId2);

    /**
     * 根据用户ID查找其所有好友信息
     * 
     * @param userId 用户ID
     * @return 好友信息视图对象列表
     */
    List<FriendVO> findFriendByUserId(Long userId);

    /**
     * 获取用户的所有好友实体对象
     * 
     * @param userId 用户ID
     * @return 好友实体对象列表
     */
    List<Friend> getFriendByUserId(Long userId);

    /**
     * 添加好友关系
     * 注意：会同时建立双向的好友关系
     * 
     * @param friendId 要添加的好友ID
     */
    void addFriend(Long friendId);

    /**
     * 删除好友关系
     * 
     * @param friendId 要删除的好友ID
     */
    void delFriend(Long friendId);

    /**
     * 更新好友信息
     * 
     * @param vo 好友信息视图对象
     */
    void update(FriendVO vo);

    /**
     * 查找特定好友关系
     * 
     * @param friendId 好友ID
     * @return 好友信息视图对象
     */
    FriendVO findFriend(Long friendId);
}
