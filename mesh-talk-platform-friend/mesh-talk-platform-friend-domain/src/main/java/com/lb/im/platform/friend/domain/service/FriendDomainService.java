package com.lb.im.platform.friend.domain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lb.im.platform.common.model.entity.Friend;
import com.lb.im.platform.common.model.vo.FriendVO;
import com.lb.im.platform.friend.domain.model.command.FriendCommand;

import java.util.List;

/**
 * 好友领域服务接口
 * 该接口定义了好友关系的核心业务逻辑
 * 
 * 技术点：
 * 1. 基于DDD(领域驱动设计)架构，将好友关系管理的核心业务逻辑封装在领域层
 * 2. 继承MyBatis-Plus的IService接口，获得通用的CRUD操作能力
 * 3. 使用命令模式（Command Pattern）传递操作参数
 * 4. 定义了完整的好友关系生命周期管理方法（查询、添加、删除、更新）
 */
public interface FriendDomainService extends IService<Friend> {

    /**
     * 获取用户的好友ID列表
     * 用于快速获取用户所有好友的ID，常用于权限检查和关系验证
     * 
     * @param userId 用户ID
     * @return 好友ID列表
     */
    List<Long> getFriendIdList(Long userId);

    /**
     * 根据用户ID查找其所有好友信息
     * 获取用户的完整好友列表，包含好友的基本信息
     * 
     * @param userId 用户ID
     * @return 好友信息列表，包含好友ID、昵称、头像等信息
     */
    List<FriendVO> findFriendByUserId(Long userId);

    /**
     * 判断两个用户是否为好友关系
     * 快速验证两个用户之间是否存在好友关系
     * 
     * @param userId1 用户ID1
     * @param userId2 用户ID2
     * @return 如果是好友返回true，否则返回false
     */
    Boolean isFriend(Long userId1, Long userId2);

    /**
     * 建立好友关系
     * 在系统中创建新的好友关系，并发布好友绑定事件
     * 
     * @param friendCommand 好友命令对象，包含用户ID和好友ID
     * @param headImg 好友头像URL
     * @param nickName 好友昵称
     */
    void bindFriend(FriendCommand friendCommand, String headImg, String nickName);

    /**
     * 解除好友关系
     * 删除系统中的好友关系，并发布好友解绑事件
     * 
     * @param friendCommand 好友命令对象，包含用户ID和好友ID
     */
    void unbindFriend(FriendCommand friendCommand);

    /**
     * 更新好友信息
     * 修改好友的昵称、头像等信息，并发布好友信息更新事件
     * 
     * @param vo 好友信息视图对象，包含要更新的信息
     * @param userId 当前操作的用户ID
     */
    void update(FriendVO vo, Long userId);

    /**
     * 查找特定好友关系
     * 根据用户ID和好友ID查询特定的好友关系详情
     * 
     * @param friendCommand 好友命令对象，包含用户ID和好友ID
     * @return 好友信息视图对象，包含好友的详细信息
     */
    FriendVO findFriend(FriendCommand friendCommand);

    /**
     * 根据用户ID获取完整的好友实体对象列表
     * 与findFriendByUserId不同，此方法返回完整的Friend实体对象
     * 
     * @param userId 用户ID
     * @return 好友实体对象列表，包含完整的好友关系数据
     */
    List<Friend> getFriendByUserId(Long userId);
}
