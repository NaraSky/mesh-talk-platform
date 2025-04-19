package com.lb.im.platform.message.application.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.lb.im.common.cache.id.SnowFlakeFactory;
import com.lb.im.common.mq.MessageSenderService;
import com.lb.im.platform.common.exception.IMException;
import com.lb.im.platform.common.model.constants.IMPlatformConstants;
import com.lb.im.platform.common.model.dto.GroupMessageDTO;
import com.lb.im.platform.common.model.enums.HttpCode;
import com.lb.im.platform.common.model.params.GroupParams;
import com.lb.im.platform.common.model.vo.GroupMemberSimpleVO;
import com.lb.im.platform.common.session.SessionContext;
import com.lb.im.platform.common.session.UserSession;
import com.lb.im.platform.dubbo.group.GroupDubboService;
import com.lb.im.platform.message.application.service.GroupMessageService;
import com.lb.im.platform.message.domain.event.IMGroupMessageTxEvent;
import com.lb.im.platform.message.domain.service.GroupMessageDomainService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 群聊消息应用服务实现类
 * 实现了群聊消息的发送、保存和查询等核心业务功能
 * 协调领域服务和外部服务完成跨服务的复杂业务逻辑
 */
@Service
public class GroupMessageServiceImpl implements GroupMessageService {
    private final Logger logger = LoggerFactory.getLogger(GroupMessageServiceImpl.class);

    // 消息发送服务，用于向消息队列发送事务消息
    @Autowired
    private MessageSenderService messageSenderService;
    
    // 群聊消息领域服务，处理消息的核心业务逻辑
    @Autowired
    private GroupMessageDomainService groupMessageDomainService;
    
    // 群组Dubbo服务，提供跨服务的群组相关操作
    @DubboReference(version = IMPlatformConstants.DEFAULT_DUBBO_VERSION, check = false)
    private GroupDubboService groupDubboService;

    /**
     * 发送群聊消息
     * 完整处理群聊消息的发送流程，包括权限验证、事务消息发送
     *
     * @param dto 群聊消息数据传输对象
     * @return 生成的消息ID
     * @throws IMException 当消息发送条件不满足时抛出异常
     */
    @Override
    public Long sendMessage(GroupMessageDTO dto) {
        // 获取当前用户会话
        UserSession userSession = SessionContext.getSession();
        
        // 检查群组是否存在
        boolean isExists = groupDubboService.isExists(dto.getGroupId());
        if (!isExists) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "群组不存在或者已经解散");
        }
        
        // 检查当前用户是否在群组中
        GroupMemberSimpleVO groupMemberSimpleVO = null;
        try {
            groupMemberSimpleVO = groupDubboService.getGroupMemberSimpleVO(new GroupParams(userSession.getUserId(), dto.getGroupId()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 验证用户是否有权发送消息
        if (Objects.isNull(groupMemberSimpleVO) || groupMemberSimpleVO.getQuit()) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "您已不在群聊里面，无法发送消息");
        }
        
        // 获取群组中的所有成员ID列表
        List<Long> userIds = groupDubboService.getUserIdsByGroupId(dto.getGroupId());
        if (CollectionUtil.isEmpty(userIds)) {
            userIds = Collections.emptyList();
        }
        
        // 过滤掉发送者自己，消息不需要发给自己
        userIds = userIds.stream()
                         .filter(id -> !userSession.getUserId().equals(id))
                         .collect(Collectors.toList());
        
        // 使用雪花算法生成唯一消息ID
        Long messageId = SnowFlakeFactory.getSnowFlakeFromCache().nextId();
        
        // 构造群聊消息事务事件
        IMGroupMessageTxEvent imGroupMessageTxEvent = new IMGroupMessageTxEvent(
                messageId,                               // 消息ID
                userSession.getUserId(),                 // 发送者ID
                groupMemberSimpleVO.getAliasName(),      // 发送者在群中的昵称
                userSession.getTerminal(),               // 发送终端类型
                new Date(),                              // 发送时间
                IMPlatformConstants.TOPIC_GROUP_TX_MESSAGE, // 消息主题
                userIds,                                 // 接收者ID列表
                dto                                      // 消息内容
        );
        
        // 发送事务消息到消息队列
        TransactionSendResult sendResult = messageSenderService.sendMessageInTransaction(imGroupMessageTxEvent, null);
        
        // 检查消息发送状态
        if (sendResult.getSendStatus() != SendStatus.SEND_OK) {
            logger.error("GroupMessageServiceImpl|发送事务消息失败|参数:{}", JSONObject.toJSONString(dto));
        }
        
        // 返回消息ID
        return messageId;
    }

    /**
     * 保存群聊消息事务事件
     * 在本地事务中将消息持久化到数据库
     *
     * @param imGroupMessageTxEvent 群聊消息事务事件
     * @return 保存是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveIMGroupMessageTxEvent(IMGroupMessageTxEvent imGroupMessageTxEvent) {
        // 调用领域服务保存消息
        return groupMessageDomainService.saveIMGroupMessageTxEvent(imGroupMessageTxEvent);
    }

    /**
     * 检查消息是否存在
     * 用于分布式事务的检查阶段
     *
     * @param messageId 消息ID
     * @return 如果消息存在返回true，否则返回false
     */
    @Override
    public boolean checkExists(Long messageId) {
        // 调用领域服务检查消息是否存在
        return groupMessageDomainService.checkExists(messageId);
    }
}
