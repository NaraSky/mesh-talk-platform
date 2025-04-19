package com.lb.im.platform.friend.domain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lb.im.common.cache.id.SnowFlakeFactory;
import com.lb.im.platform.common.exception.IMException;
import com.lb.im.platform.common.model.constants.IMPlatformConstants;
import com.lb.im.platform.common.model.entity.Friend;
import com.lb.im.platform.common.model.enums.HttpCode;
import com.lb.im.platform.common.model.vo.FriendVO;
import com.lb.im.platform.friend.domain.model.command.FriendCommand;
import com.lb.im.platform.friend.domain.repository.FriendRepository;
import com.lb.im.platform.friend.domain.service.FriendDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 好友领域服务实现类
 * 实现了好友关系的核心业务逻辑
 * 技术：
 * 1. 基于DDD架构设计
 * 2. 使用MyBatis-Plus的ServiceImpl作为基类
 * 3. 使用雪花算法生成ID
 * 4. 预留了领域事件发布机制（TODO标记处）
 */
@Service
public class FriendDomainServiceImpl extends ServiceImpl<FriendRepository, Friend> implements FriendDomainService {

    private final Logger logger = LoggerFactory.getLogger(FriendDomainServiceImpl.class);

    /**
     * 消息队列事件类型，通过配置文件注入
     */
    @Value("${message.mq.event.type}")
    private String eventType;

    /**
     * 获取用户的好友ID列表
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
     * 
     * @param userId 用户ID
     * @return 好友信息列表
     * @throws IMException 当参数错误时抛出异常
     */
    @Override
    public List<FriendVO> findFriendByUserId(Long userId) {
        if (userId == null){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        return baseMapper.getFriendVOList(userId);
    }

    /**
     * 判断两个用户是否为好友关系
     * 
     * @param userId1 用户ID1
     * @param userId2 用户ID2
     * @return 如果是好友返回true，否则返回false
     * @throws IMException 当参数错误时抛出异常
     */
    @Override
    public Boolean isFriend(Long userId1, Long userId2) {
        if (userId1 == null || userId2 == null){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        return baseMapper.checkFriend(userId2, userId1) != null;
    }

    /**
     * 建立好友关系
     * 
     * @param friendCommand 好友命令对象，包含用户ID和好友ID
     * @param headImg 好友头像
     * @param nickName 好友昵称
     * @throws IMException 当参数错误时抛出异常
     */
    @Override
    public void bindFriend(FriendCommand friendCommand, String headImg, String nickName) {
        if (friendCommand == null || friendCommand.isEmpty()){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        boolean result = false;
        // 检查是否已经是好友
        Integer checkStatus = baseMapper.checkFriend(friendCommand.getFriendId(), friendCommand.getUserId());
        if(checkStatus == null){
            // 创建好友关系
            Friend friend = new Friend();
            // 使用雪花算法生成ID
            friend.setId(SnowFlakeFactory.getSnowFlakeFromCache().nextId());
            friend.setUserId(friendCommand.getUserId());
            friend.setFriendId(friendCommand.getFriendId());
            friend.setFriendHeadImage(headImg);
            friend.setFriendNickName(nickName);
            result = this.save(friend);
        }
        if (result){
            //TODO 发布领域事件
        }
    }

    /**
     * 解除好友关系
     * 
     * @param friendCommand 好友命令对象，包含用户ID和好友ID
     * @throws IMException 当参数错误时抛出异常
     */
    @Override
    public void unbindFriend(FriendCommand friendCommand) {
        if (friendCommand == null || friendCommand.isEmpty()){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        int count = baseMapper.deleteFriend(friendCommand.getFriendId(), friendCommand.getUserId());
        if (count > 0){
            //TODO 发布领域事件
        }
    }

    /**
     * 更新好友信息
     * 
     * @param vo 好友信息视图对象
     * @param userId 用户ID
     */
    @Override
    public void update(FriendVO vo, Long userId) {
        int count = baseMapper.updateFriend(vo.getHeadImage(), vo.getNickName(), vo.getId(), userId);
        if (count > 0){
            //TODO 发布领域事件
        }
    }

    /**
     * 查找特定好友关系
     * 
     * @param friendCommand 好友命令对象，包含用户ID和好友ID
     * @return 好友信息视图对象
     * @throws IMException 当参数错误时抛出异常
     */
    @Override
    public FriendVO findFriend(FriendCommand friendCommand) {
        if (friendCommand == null || friendCommand.isEmpty()){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        return baseMapper.getFriendVO(friendCommand.getFriendId(), friendCommand.getUserId());
    }

    /**
     * 获取主题事件
     * 根据配置的消息队列类型返回对应的主题
     * 
     * @return 主题事件名称
     */
    private String getTopicEvent(){
        return IMPlatformConstants.EVENT_PUBLISH_TYPE_ROCKETMQ.equals(eventType) ? IMPlatformConstants.TOPIC_EVENT_ROCKETMQ_FRIEND : IMPlatformConstants.TOPIC_EVENT_COLA;
    }
}
