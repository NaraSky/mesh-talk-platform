package com.lb.im.platform.friend.domain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lb.im.platform.common.model.entity.Friend;
import com.lb.im.platform.common.model.vo.FriendVO;
import com.lb.im.platform.friend.domain.model.command.FriendCommand;

import java.util.List;

/**
 * 好友领域服务接口
 * 该接口定义了好友关系的核心业务逻辑
 * 技术：基于DDD(领域驱动设计)架构，继承MyBatis-Plus的IService接口
 */
public interface FriendDomainService extends IService<Friend> {

    /**
     * 获取用户的好友ID列表
     * 
     * @param userId 用户ID
     * @return 好友ID列表
     */
    List<Long> getFriendIdList(Long userId);

    /**
     * 根据用户ID查找其所有好友信息
     * 
     * @param userId 用户ID
     * @return 好友信息列表
     */
    List<FriendVO> findFriendByUserId(Long userId);

    /**
     * 判断两个用户是否为好友关系
     * 
     * @param userId1 用户ID1
     * @param userId2 用户ID2
     * @return 如果是好友返回true，否则返回false
     */
    Boolean isFriend(Long userId1, Long userId2);

    /**
     * 建立好友关系
     * 
     * @param friendCommand 好友命令对象，包含用户ID和好友ID
     * @param headImg 好友头像
     * @param nickName 好友昵称
     */
    void bindFriend(FriendCommand friendCommand, String headImg, String nickName);

    /**
     * 解除好友关系
     * 
     * @param friendCommand 好友命令对象，包含用户ID和好友ID
     */
    void unbindFriend(FriendCommand friendCommand);

    /**
     * 更新好友信息
     * 
     * @param vo 好友信息视图对象
     * @param userId 用户ID
     */
    void update(FriendVO vo, Long userId);

    /**
     * 查找特定好友关系
     * 
     * @param friendCommand 好友命令对象，包含用户ID和好友ID
     * @return 好友信息视图对象
     */
    FriendVO findFriend(FriendCommand friendCommand);
}
