package com.lb.im.platform.message.application.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.BooleanUtil;
import com.alibaba.fastjson.JSONObject;
import com.lb.im.common.cache.id.SnowFlakeFactory;
import com.lb.im.common.domain.model.IMPrivateMessage;
import com.lb.im.common.domain.model.IMUserInfo;
import com.lb.im.common.mq.MessageSenderService;
import com.lb.im.platform.common.exception.IMException;
import com.lb.im.platform.common.model.constants.IMPlatformConstants;
import com.lb.im.platform.common.model.dto.PrivateMessageDTO;
import com.lb.im.platform.common.model.enums.HttpCode;
import com.lb.im.platform.common.model.enums.MessageStatus;
import com.lb.im.platform.common.model.vo.PrivateMessageVO;
import com.lb.im.platform.common.session.SessionContext;
import com.lb.im.platform.common.session.UserSession;
import com.lb.im.platform.common.threadpool.PrivateMessageThreadPoolUtils;
import com.lb.im.platform.common.utils.DateTimeUtils;
import com.lb.im.platform.dubbo.friend.FriendDubboService;
import com.lb.im.platform.message.application.service.PrivateMessageService;
import com.lb.im.platform.message.domain.event.IMPrivateMessageTxEvent;
import com.lb.im.platform.message.domain.service.PrivateMessageDomainService;
import com.lb.im.sdk.client.IMClient;
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
import java.util.stream.Collectors;

/**
 * 私聊消息服务实现类
 * 功能：
 * 1. 处理私聊消息的发送、存储和查询
 * 2. 支持未读消息拉取和历史消息查询
 * 3. 使用事务消息确保消息的可靠发送
 */
@Service
public class PrivateMessageServiceImpl implements PrivateMessageService {

    private final Logger logger = LoggerFactory.getLogger(PrivateMessageServiceImpl.class);

    /**
     * 通过Dubbo远程调用好友服务
     * 用于验证好友关系和获取好友列表
     */
    @DubboReference(version = IMPlatformConstants.DEFAULT_DUBBO_VERSION, check = false)
    private FriendDubboService friendDubboService;

    /**
     * IM客户端，用于发送消息和检查用户在线状态
     */
    @Autowired
    private IMClient imClient;
    
    /**
     * 消息发送服务，用于发送事务消息
     */
    @Autowired
    private MessageSenderService messageSenderService;
    
    /**
     * 私聊消息领域服务，处理底层消息存储和查询
     */
    @Autowired
    private PrivateMessageDomainService privateMessageDomainService;

    /**
     * 发送私聊消息
     * 流程：
     * 1. 验证发送者和接收者是否为好友关系
     * 2. 生成全局唯一消息ID
     * 3. 创建事务消息并发送到消息队列
     * 
     * @param dto 私聊消息数据传输对象
     * @return 生成的消息ID
     * @throws IMException 当用户不是好友或消息发送失败时抛出
     */
    @Override
    public Long sendMessage(PrivateMessageDTO dto) {
        // 获取当前用户会话信息
        UserSession session = SessionContext.getSession();
        
        // 验证发送者和接收者是否为好友关系
        Boolean isFriend = friendDubboService.isFriend(session.getUserId(), dto.getRecvId());
        if (BooleanUtil.isFalse(isFriend)) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "对方不是你的好友，无法发送消息");
        }
        
        // 生成全局唯一消息ID
        Long messageId = SnowFlakeFactory.getSnowFlakeFromCache().nextId();
        
        // 组装事务消息数据
        IMPrivateMessageTxEvent imPrivateMessageTxEvent = new IMPrivateMessageTxEvent(messageId,
                                                                                     session.getUserId(),
                                                                                     session.getTerminal(),
                                                                                     IMPlatformConstants.TOPIC_PRIVATE_TX_MESSAGE,
                                                                                     new Date(),
                                                                                     dto);
        
        // 发送事务消息，确保消息可靠投递
        TransactionSendResult sendResult = messageSenderService.sendMessageInTransaction(imPrivateMessageTxEvent, null);
        if (sendResult.getSendStatus() != SendStatus.SEND_OK) {
            logger.error("PrivateMessageServiceImpl|发送事务消息失败|参数:{}", JSONObject.toJSONString(dto));
        }
        
        return messageId;
    }

    /**
     * 保存私聊消息事务事件
     * 在本地事务中持久化消息数据
     * 
     * @param privateMessageSaveEvent 私聊消息保存事件
     * @return 是否保存成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveIMPrivateMessageSaveEvent(IMPrivateMessageTxEvent privateMessageSaveEvent) {
        return privateMessageDomainService.saveIMPrivateMessageSaveEvent(privateMessageSaveEvent);
    }

    /**
     * 检查指定ID的消息是否存在
     * 用于事务消息的回查机制
     * 
     * @param messageId 消息ID
     * @return 消息是否存在
     */
    @Override
    public boolean checkExists(Long messageId) {
        return privateMessageDomainService.checkExists(messageId);
    }

    /**
     * 拉取当前用户的所有未读消息
     * 流程：
     * 1. 验证用户是否在线
     * 2. 获取用户的好友列表
     * 3. 查询未读消息并推送给用户
     * 
     * @throws IMException 当用户未建立连接时抛出
     */
    @Override
    public void pullUnreadMessage() {
        // 获取当前用户会话
        UserSession userSession = SessionContext.getSession();
        
        // 验证用户是否在线
        if (!imClient.isOnline(userSession.getUserId())){
            throw new IMException(HttpCode.PROGRAM_ERROR, "用户未建立连接");
        }
        
        // 获取用户的所有好友ID列表
        List<Long> friendIdList = friendDubboService.getFriendIdList(userSession.getUserId());
        if (CollectionUtil.isEmpty(friendIdList)){
            return;
        }
        
        // 获取所有未读的私聊消息
        List<PrivateMessageVO> privateMessageList = privateMessageDomainService.getPrivateMessageVOList(userSession.getUserId(), friendIdList);
        int messageSize = 0;
        
        // 如果有未读消息，则推送给用户
        if (!CollectionUtil.isEmpty(privateMessageList)){
            messageSize = privateMessageList.size();
            // 并行处理每条未读消息
            privateMessageList.parallelStream().forEach((privateMessageVO) -> {
                // 构造推送消息对象
                IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
                sendMessage.setSender(new IMUserInfo(userSession.getUserId(), userSession.getTerminal()));
                sendMessage.setReceiveId(userSession.getUserId());
                sendMessage.setReceiveTerminals(Collections.singletonList(userSession.getTerminal()));
                sendMessage.setSendToSelf(false);
                sendMessage.setData(privateMessageVO);
                
                // 通过IM客户端发送私聊消息
                imClient.sendPrivateMessage(sendMessage);
            });
        }
        
        logger.info("拉取未读私聊消息，用户id:{},数量:{}", userSession.getUserId(), messageSize);
    }

    /**
     * 加载消息历史记录
     * 支持增量拉取，只获取指定ID之后的消息
     * 
     * @param minId 最小消息ID，用于增量拉取
     * @return 消息历史记录列表
     */
    @Override
    public List<PrivateMessageVO> loadMessage(Long minId) {
        // 获取当前用户会话
        UserSession session = SessionContext.getSession();
        
        // 获取好友列表
        List<Long> friendIdList = friendDubboService.getFriendIdList(session.getUserId());
        if (CollectionUtil.isEmpty(friendIdList)){
            return Collections.emptyList();
        }
        
        // 设定查询时间范围（最近一个月）
        Date minDate = DateTimeUtils.addMonths(new Date(), -1);
        
        // 查询消息记录
        List<PrivateMessageVO> privateMessageList = privateMessageDomainService.loadMessage(
            session.getUserId(), 
            minId, 
            minDate, 
            friendIdList, 
            IMPlatformConstants.PULL_HISTORY_MESSAGE_LIMIT_COUNR
        );
        
        if (CollectionUtil.isEmpty(privateMessageList)){
            return Collections.emptyList();
        }
        
        // 异步更新消息状态为已发送
        PrivateMessageThreadPoolUtils.execute(() -> {
            // 筛选需要更新状态的消息ID
            List<Long> ids = privateMessageList.stream()
                    .filter(m -> !m.getSendId().equals(session.getUserId()) && m.getStatus().equals(MessageStatus.UNSEND.code()))
                    .map(PrivateMessageVO::getId)
                    .collect(Collectors.toList());
                    
            // 批量更新消息状态
            if (!CollectionUtil.isEmpty(ids)){
                privateMessageDomainService.batchUpdatePrivateMessageStatus(MessageStatus.SENDED.code(), ids);
            }
        });
        
        logger.info("拉取消息，用户id:{},数量:{}", session.getUserId(), privateMessageList.size());
        return privateMessageList;
    }

    /**
     * 获取与指定好友的历史聊天记录
     * 支持分页查询
     * 
     * @param friendId 好友ID
     * @param page 页码，从1开始
     * @param size 每页消息数量
     * @return 历史聊天记录列表
     */
    @Override
    public List<PrivateMessageVO> getHistoryMessage(Long friendId, Long page, Long size) {
        // 处理分页参数
        page = page > 0 ? page : 1;
        size = size > 0 ? size : 10;
        
        // 获取当前用户ID
        Long userId = SessionContext.getSession().getUserId();
        
        // 计算分页起始索引
        long stIdx = (page - 1) * size;
        
        // 获取聊天记录
        List<PrivateMessageVO> privateMessageList = privateMessageDomainService.loadMessageByUserIdAndFriendId(userId, friendId, stIdx, size);
        if (CollectionUtil.isEmpty(privateMessageList)){
            privateMessageList = Collections.emptyList();
        }
        
        logger.info("拉取聊天记录，用户id:{},好友id:{}，数量:{}", userId, friendId, privateMessageList.size());
        return privateMessageList;
    }
}