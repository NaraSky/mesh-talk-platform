package com.lb.im.platform.friend.domain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lb.im.common.cache.id.SnowFlakeFactory;
import com.lb.im.common.mq.event.MessageEventSenderService;
import com.lb.im.platform.common.exception.IMException;
import com.lb.im.platform.common.model.constants.IMPlatformConstants;
import com.lb.im.platform.common.model.entity.Friend;
import com.lb.im.platform.common.model.enums.HttpCode;
import com.lb.im.platform.common.model.vo.FriendVO;
import com.lb.im.platform.friend.domain.event.IMFriendEvent;
import com.lb.im.platform.friend.domain.model.command.FriendCommand;
import com.lb.im.platform.friend.domain.repository.FriendRepository;
import com.lb.im.platform.friend.domain.service.FriendDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 好友领域服务实现类
 * 实现了好友关系的核心业务逻辑
 * 
 * 技术点：
 * 1. 基于DDD（领域驱动设计）架构设计，实现领域层核心业务逻辑
 * 2. 使用MyBatis-Plus的ServiceImpl作为基类，简化数据访问操作
 * 3. 使用雪花算法（SnowFlake）生成分布式唯一ID
 * 4. 实现了领域事件发布机制，支持RocketMQ和COLA两种事件发布方式
 * 5. 采用依赖注入方式管理组件间依赖关系
 */
@Service
public class FriendDomainServiceImpl extends ServiceImpl<FriendRepository, Friend> implements FriendDomainService {

    private final Logger logger = LoggerFactory.getLogger(FriendDomainServiceImpl.class);

    /**
     * 消息事件发送服务
     * 用于发送领域事件到消息队列
     */
    @Autowired
    private MessageEventSenderService messageEventSenderService;

    /**
     * 消息队列事件类型，通过配置文件注入
     * 支持"rocketmq"和"cola"两种类型
     */
    @Value("${message.mq.event.type}")
    private String eventType;

    /**
     * 获取用户的好友ID列表
     * 直接调用Repository层方法获取数据
     *
     * @param userId 用户ID
     * @return 好友ID列表
     */
    @Override
    public List<Long> getFriendIdList(Long userId) {
        return baseMapper.getFriendIdList(userId);
    }

    /**
     * 根据用户ID查找其所有好友信息
     * 包含参数校验和数据查询逻辑
     *
     * @param userId 用户ID
     * @return 好友信息列表
     * @throws IMException 当参数错误时抛出异常
     */
    @Override
    public List<FriendVO> findFriendByUserId(Long userId) {
        // 参数校验
        if (userId == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        // 查询好友列表
        return baseMapper.getFriendVOList(userId);
    }

    /**
     * 判断两个用户是否为好友关系
     * 通过查询数据库判断好友关系是否存在
     *
     * @param userId1 用户ID1
     * @param userId2 用户ID2
     * @return 如果是好友返回true，否则返回false
     * @throws IMException 当参数错误时抛出异常
     */
    @Override
    public Boolean isFriend(Long userId1, Long userId2) {
        // 参数校验
        if (userId1 == null || userId2 == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        // 查询好友关系
        return baseMapper.checkFriend(userId2, userId1) != null;
    }

    /**
     * 建立好友关系
     * 创建新的好友关系记录，并发布好友绑定事件
     *
     * @param friendCommand 好友命令对象，包含用户ID和好友ID
     * @param headImg       好友头像
     * @param nickName      好友昵称
     * @throws IMException 当参数错误时抛出异常
     */
    @Override
    public void bindFriend(FriendCommand friendCommand, String headImg, String nickName) {
        // 参数校验
        if (friendCommand == null || friendCommand.isEmpty()) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        boolean result = false;
        // 检查是否已经是好友
        Integer checkStatus = baseMapper.checkFriend(friendCommand.getFriendId(), friendCommand.getUserId());
        if (checkStatus == null) {
            // 创建好友关系实体
            Friend friend = new Friend();
            // 使用雪花算法生成分布式唯一ID
            friend.setId(SnowFlakeFactory.getSnowFlakeFromCache().nextId());
            friend.setUserId(friendCommand.getUserId());
            friend.setFriendId(friendCommand.getFriendId());
            friend.setFriendHeadImage(headImg);
            friend.setFriendNickName(nickName);
            // 保存到数据库
            result = this.save(friend);
        }
        // 如果保存成功，发布领域事件
        if (result) {
            // 创建并发送好友绑定事件
            IMFriendEvent friendEvent = new IMFriendEvent(friendCommand.getUserId(), friendCommand.getFriendId(),
                                                          IMPlatformConstants.FRIEND_HANDLER_BIND, this.getTopicEvent());
            messageEventSenderService.send(friendEvent);
        }
    }

    /**
     * 解除好友关系
     * 删除好友关系记录，并发布好友解绑事件
     *
     * @param friendCommand 好友命令对象，包含用户ID和好友ID
     * @throws IMException 当参数错误时抛出异常
     */
    @Override
    public void unbindFriend(FriendCommand friendCommand) {
        // 参数校验
        if (friendCommand == null || friendCommand.isEmpty()) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        // 删除好友关系
        int count = baseMapper.deleteFriend(friendCommand.getFriendId(), friendCommand.getUserId());
        // 如果删除成功，发布领域事件
        if (count > 0) {
            // 创建并发送好友解绑事件
            IMFriendEvent friendEvent = new IMFriendEvent(friendCommand.getUserId(), friendCommand.getFriendId(),
                                                          IMPlatformConstants.FRIEND_HANDLER_UNBIND, this.getTopicEvent());
            messageEventSenderService.send(friendEvent);
        }
    }

    /**
     * 更新好友信息
     * 更新好友昵称和头像，并发布好友信息更新事件
     *
     * @param vo     好友信息视图对象
     * @param userId 用户ID
     */
    @Override
    public void update(FriendVO vo, Long userId) {
        // 更新好友信息
        int count = baseMapper.updateFriend(vo.getHeadImage(), vo.getNickName(), vo.getId(), userId);
        // 如果更新成功，发布领域事件
        if (count > 0) {
            // 创建并发送好友信息更新事件
            IMFriendEvent friendEvent = new IMFriendEvent(userId, vo.getId(), IMPlatformConstants.FRIEND_HANDLER_UPDATE, this.getTopicEvent());
            messageEventSenderService.send(friendEvent);
        }
    }

    /**
     * 查找特定好友关系
     * 根据用户ID和好友ID查询好友关系详情
     *
     * @param friendCommand 好友命令对象，包含用户ID和好友ID
     * @return 好友信息视图对象
     * @throws IMException 当参数错误时抛出异常
     */
    @Override
    public FriendVO findFriend(FriendCommand friendCommand) {
        // 参数校验
        if (friendCommand == null || friendCommand.isEmpty()) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        // 查询好友关系
        return baseMapper.getFriendVO(friendCommand.getFriendId(), friendCommand.getUserId());
    }

    /**
     * 根据用户ID获取所有好友实体对象
     * 
     * @param userId 用户ID
     * @return 好友实体对象列表
     */
    @Override
    public List<Friend> getFriendByUserId(Long userId) {
        return baseMapper.getFriendByUserId(userId);
    }

    /**
     * 获取主题事件名称
     * 根据配置的消息队列类型返回对应的主题名称
     * 支持RocketMQ和COLA两种事件发布方式
     *
     * @return 主题事件名称
     */
    private String getTopicEvent() {
        return IMPlatformConstants.EVENT_PUBLISH_TYPE_ROCKETMQ.equals(eventType) ? 
               IMPlatformConstants.TOPIC_EVENT_ROCKETMQ_FRIEND : 
               IMPlatformConstants.TOPIC_EVENT_COLA;
    }
}
