package com.lb.im.platform.message.application.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.BooleanUtil;
import com.alibaba.fastjson.JSONObject;
import com.lb.im.common.cache.id.SnowFlakeFactory;
import com.lb.im.common.cache.time.SystemClock;
import com.lb.im.common.domain.constans.IMConstants;
import com.lb.im.common.domain.model.IMPrivateMessage;
import com.lb.im.common.domain.model.IMUserInfo;
import com.lb.im.common.mq.MessageSenderService;
import com.lb.im.platform.common.exception.IMException;
import com.lb.im.platform.common.model.constants.IMPlatformConstants;
import com.lb.im.platform.common.model.dto.PrivateMessageDTO;
import com.lb.im.platform.common.model.enums.HttpCode;
import com.lb.im.platform.common.model.enums.MessageStatus;
import com.lb.im.platform.common.model.enums.MessageType;
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
 * 私聊消息应用服务实现类
 * 实现了私聊消息的发送、保存和查询等核心业务功能
 * 协调领域服务和外部服务完成跨服务的复杂业务逻辑
 * 
 * 主要功能：
 * 1. 处理私聊消息的发送、存储和查询
 * 2. 支持未读消息拉取和历史消息查询
 * 3. 使用事务消息确保消息的可靠发送
 * 4. 提供消息已读和消息撤回功能
 * 5. 通过WebSocket实时推送消息状态变更
 */
@Service
public class PrivateMessageServiceImpl implements PrivateMessageService {

    private final Logger logger = LoggerFactory.getLogger(PrivateMessageServiceImpl.class);
    @Autowired
    private IMClient imClient;
    @Autowired
    private MessageSenderService messageSenderService;
    @Autowired
    private PrivateMessageDomainService privateMessageDomainService;
    @DubboReference(version = IMPlatformConstants.DEFAULT_DUBBO_VERSION, check = false)
    private FriendDubboService friendDubboService;

    /**
     * 发送私聊消息
     * 完整处理私聊消息的发送流程，包括权限验证、事务消息发送
     * 
     * 实现步骤：
     * 1. 获取当前用户会话信息
     * 2. 验证发送者和接收者是否为好友关系
     * 3. 生成全局唯一的消息ID
     * 4. 构建私聊消息事务事件
     * 5. 发送事务消息到消息队列
     * 6. 返回生成的消息ID
     *
     * @param dto 私聊消息数据传输对象，包含消息内容、接收者ID等信息
     * @return 生成的消息ID
     * @throws IMException 当消息发送条件不满足时抛出异常，如非好友关系
     */
    @Override
    public Long sendMessage(PrivateMessageDTO dto) {
        // 获取当前用户会话
        UserSession session = SessionContext.getSession();

        // 验证发送者和接收者是否为好友关系
        Boolean isFriend = friendDubboService.isFriend(session.getUserId(), dto.getRecvId());
        if (BooleanUtil.isFalse(isFriend)) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "对方不是你的好友，无法发送消息");
        }

        // 使用雪花算法生成全局唯一的消息ID
        Long messageId = SnowFlakeFactory.getSnowFlakeFromCache().nextId();

        // 组装事务消息数据
        IMPrivateMessageTxEvent imPrivateMessageTxEvent = new IMPrivateMessageTxEvent(messageId,
                                                                                      session.getUserId(),
                                                                                      session.getTerminal(),
                                                                                      IMPlatformConstants.TOPIC_PRIVATE_TX_MESSAGE,
                                                                                      new Date(),
                                                                                      dto);

        // 发送事务消息到消息队列
        TransactionSendResult sendResult = messageSenderService.sendMessageInTransaction(imPrivateMessageTxEvent, null);
        if (sendResult.getSendStatus() != SendStatus.SEND_OK) {
            logger.error("PrivateMessageServiceImpl|发送事务消息失败|参数:{}", JSONObject.toJSONString(dto));
        }

        // 返回生成的消息ID
        return messageId;
    }

    /**
     * 保存私聊消息事务事件
     * 在本地事务中将消息持久化到数据库
     * 
     * 实现方式：
     * 使用Spring事务注解确保数据一致性，调用领域服务完成消息保存
     *
     * @param privateMessageSaveEvent 私聊消息事务事件，包含消息的完整信息
     * @return 保存操作是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveIMPrivateMessageSaveEvent(IMPrivateMessageTxEvent privateMessageSaveEvent) {
        return privateMessageDomainService.saveIMPrivateMessageSaveEvent(privateMessageSaveEvent);
    }

    /**
     * 检查消息是否存在
     * 用于分布式事务的检查阶段，确认消息是否已成功持久化
     * 
     * 实现方式：
     * 直接调用领域服务的检查方法
     *
     * @param messageId 要检查的消息ID
     * @return 如果消息存在返回true，否则返回false
     */
    @Override
    public boolean checkExists(Long messageId) {
        return privateMessageDomainService.checkExists(messageId);
    }

    /**
     * 异步拉取未读私聊消息
     * 获取当前用户的所有未读私聊消息，并通过WebSocket异步推送给用户
     * 
     * 实现步骤：
     * 1. 获取当前用户会话信息
     * 2. 验证用户是否在线（已建立WebSocket连接）
     * 3. 获取用户的好友ID列表
     * 4. 查询所有未读消息
     * 5. 并行处理每条消息，通过WebSocket推送给用户
     * 6. 记录日志
     * 
     * @throws IMException 当用户未建立WebSocket连接时抛出异常
     */
    @Override
    public void pullUnreadMessage() {
        // 获取当前用户会话
        UserSession userSession = SessionContext.getSession();

        // 验证用户是否在线
        if (!imClient.isOnline(userSession.getUserId())) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "用户未建立连接");
        }

        // 获取用户的好友ID列表
        List<Long> friendIdList = friendDubboService.getFriendIdList(userSession.getUserId());
        if (CollectionUtil.isEmpty(friendIdList)) {
            return;
        }

        // 查询所有未读消息
        List<PrivateMessageVO> privateMessageList = privateMessageDomainService.getPrivateMessageVOList(userSession.getUserId(), friendIdList);
        int messageSize = 0;

        // 处理未读消息
        if (!CollectionUtil.isEmpty(privateMessageList)) {
            messageSize = privateMessageList.size();
            // 并行处理每条消息，提高性能
            privateMessageList.parallelStream().forEach((privateMessageVO) -> {
                // 构建推送消息对象
                IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
                sendMessage.setSender(new IMUserInfo(userSession.getUserId(), userSession.getTerminal()));
                sendMessage.setReceiveId(userSession.getUserId());
                sendMessage.setReceiveTerminals(Collections.singletonList(userSession.getTerminal()));
                sendMessage.setSendToSelf(false);
                sendMessage.setData(privateMessageVO);
                // 通过WebSocket推送消息
                imClient.sendPrivateMessage(sendMessage);
            });
        }

        // 记录日志
        logger.info("拉取未读私聊消息，用户id:{},数量:{}", userSession.getUserId(), messageSize);
    }

    /**
     * 加载消息历史记录
     * 增量拉取指定ID之后的私聊消息，限制为最近1个月内的消息
     * 
     * 实现步骤：
     * 1. 获取当前用户会话信息
     * 2. 获取用户的好友ID列表
     * 3. 设置时间范围限制（最近1个月）
     * 4. 查询符合条件的消息历史记录
     * 5. 异步更新接收到的消息状态为已发送
     * 6. 记录日志并返回消息列表
     *
     * @param minId 最小消息ID，用于增量拉取，返回ID大于此值的消息
     * @return 消息历史记录VO对象列表，按消息ID升序排序，最多返回100条
     */
    @Override
    public List<PrivateMessageVO> loadMessage(Long minId) {
        // 获取当前用户会话
        UserSession session = SessionContext.getSession();

        // 获取用户的好友ID列表
        List<Long> friendIdList = friendDubboService.getFriendIdList(session.getUserId());
        if (CollectionUtil.isEmpty(friendIdList)) {
            return Collections.emptyList();
        }

        // 设置时间范围限制（最近1个月）
        Date minDate = DateTimeUtils.addMonths(new Date(), -1);

        // 查询符合条件的消息历史记录
        List<PrivateMessageVO> privateMessageList = privateMessageDomainService.loadMessage(
            session.getUserId(), 
            minId, 
            minDate, 
            friendIdList, 
            IMPlatformConstants.PULL_HISTORY_MESSAGE_LIMIT_COUNR
        );

        if (CollectionUtil.isEmpty(privateMessageList)) {
            return Collections.emptyList();
        }

        // 异步更新接收到的消息状态为已发送
        PrivateMessageThreadPoolUtils.execute(() -> {
            // 筛选出需要更新状态的消息ID列表（接收的且未读的消息）
            List<Long> ids = privateMessageList.stream()
                    .filter(m -> !m.getSendId().equals(session.getUserId()) && m.getStatus().equals(MessageStatus.UNSEND.code()))
                    .map(PrivateMessageVO::getId)
                    .collect(Collectors.toList());

            if (!CollectionUtil.isEmpty(ids)) {
                // 批量更新消息状态为已发送
                privateMessageDomainService.batchUpdatePrivateMessageStatus(MessageStatus.SENDED.code(), ids);
            }
        });

        // 记录日志
        logger.info("拉取消息，用户id:{},数量:{}", session.getUserId(), privateMessageList.size());

        return privateMessageList;
    }

    /**
     * 获取与指定好友的历史聊天记录
     * 分页获取与特定好友的历史私聊消息
     * 
     * 实现步骤：
     * 1. 处理分页参数，设置默认值
     * 2. 获取当前用户ID
     * 3. 计算分页起始索引
     * 4. 查询指定好友的历史消息
     * 5. 记录日志并返回消息列表
     *
     * @param friendId 好友用户ID
     * @param page 页码，从1开始，如果小于1则使用默认值1
     * @param size 每页大小，如果小于1则使用默认值10
     * @return 历史消息VO对象列表，按消息ID倒序排序
     */
    @Override
    public List<PrivateMessageVO> getHistoryMessage(Long friendId, Long page, Long size) {
        // 处理分页参数，设置默认值
        page = page > 0 ? page : IMPlatformConstants.DEFAULT_PAGE;
        size = size > 0 ? size : IMPlatformConstants.DEFAULT_PAGE_SIZE;

        // 获取当前用户ID
        Long userId = SessionContext.getSession().getUserId();

        // 计算分页起始索引
        long stIdx = (page - 1) * size;

        // 查询指定好友的历史消息
        List<PrivateMessageVO> privateMessageList = privateMessageDomainService.loadMessageByUserIdAndFriendId(userId, friendId, stIdx, size);
        if (CollectionUtil.isEmpty(privateMessageList)) {
            privateMessageList = Collections.emptyList();
        }

        // 记录日志
        logger.info("拉取聊天记录，用户id:{},好友id:{}，数量:{}", userId, friendId, privateMessageList.size());

        return privateMessageList;
    }

    /**
     * 标记消息为已读状态
     * 将与指定好友的整个会话中的所有消息都置为已读状态
     * 同时会通知其他终端同步已读状态
     * 
     * 实现步骤：
     * 1. 验证参数有效性
     * 2. 获取当前用户会话
     * 3. 构建已读状态消息
     * 4. 通过WebSocket推送已读状态消息
     * 5. 异步更新数据库中的消息状态
     * 6. 记录日志
     *
     * @param friendId 好友用户ID，消息发送者
     * @throws IMException 当参数无效时抛出异常
     */
    @Override
    public void readedMessage(Long friendId) {
        // 验证参数有效性
        if (friendId == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }

        // 获取当前用户会话
        UserSession session = SessionContext.getSession();

        // 构建已读状态消息
        PrivateMessageVO msgInfo = new PrivateMessageVO();
        msgInfo.setType(MessageType.READED.code());  // 设置消息类型为已读
        msgInfo.setSendTime(new Date());  // 设置当前时间
        msgInfo.setSendId(session.getUserId());  // 设置发送者为当前用户
        msgInfo.setRecvId(friendId);  // 设置接收者为好友

        // 构建推送消息对象
        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setReceiveId(friendId);  // 发送给好友
        sendMessage.setSendToSelf(true);  // 同时发送给自己的其他终端
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);  // 不需要发送结果

        // 通过WebSocket推送已读状态消息
        imClient.sendPrivateMessage(sendMessage);

        // 异步更新数据库中的消息状态
        PrivateMessageThreadPoolUtils.execute(() -> {
            privateMessageDomainService.updateMessageStatus(MessageStatus.READED.code(), friendId, session.getUserId());
        });

        // 记录日志
        logger.info("消息已读，接收方id:{},发送方id:{}", session.getUserId(), friendId);
    }

    /**
     * 撤回消息
     * 将指定ID的消息标记为已撤回状态
     * 撤回的消息在客户端会显示为"消息已撤回"
     * 
     * 实现步骤：
     * 1. 获取当前用户会话
     * 2. 验证参数有效性
     * 3. 获取要撤回的消息详情
     * 4. 验证撤回权限（只能撤回自己发送的消息）
     * 5. 验证撤回时间限制（5分钟内可撤回）
     * 6. 更新消息状态为已撤回
     * 7. 异步推送撤回通知给接收方和自己的其他终端
     * 8. 记录日志
     *
     * @param id 要撤回的消息ID
     * @throws IMException 当参数无效、消息不存在、无权撤回或超时时抛出异常
     */
    @Override
    public void recallMessage(Long id) {
        // 获取当前用户会话
        UserSession session = SessionContext.getSession();

        // 验证参数有效性
        if (id == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }

        // 获取要撤回的消息详情
        PrivateMessageVO privateMessage = privateMessageDomainService.getPrivateMessageById(id);
        if (privateMessage == null) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "消息不存在");
        }

        // 验证撤回权限（只能撤回自己发送的消息）
        if (!privateMessage.getSendId().equals(session.getUserId())) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "这条消息不是由您发送,无法撤回");
        }

        // 验证撤回时间限制（5分钟内可撤回）
        if (SystemClock.millisClock().now() - privateMessage.getSendTime().getTime() > IMConstants.ALLOW_RECALL_SECOND * 1000) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "消息已发送超过5分钟，无法撤回");
        }

        // 更新消息状态为已撤回
        privateMessageDomainService.updateMessageStatusById(MessageStatus.RECALL.code(), id);

        // 异步推送撤回通知
        PrivateMessageThreadPoolUtils.execute(() -> {
            // 构建推送给接收方的撤回消息
            privateMessage.setType(MessageType.RECALL.code());  // 设置消息类型为撤回
            privateMessage.setSendTime(new Date());  // 设置当前时间
            privateMessage.setContent("对方撤回了一条消息");  // 设置撤回提示文本

            // 构建推送消息对象
            IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
            sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
            sendMessage.setReceiveId(privateMessage.getRecvId());  // 发送给原消息接收者
            sendMessage.setSendToSelf(false);  // 不发送给自己
            sendMessage.setData(privateMessage);
            sendMessage.setSendResult(false);  // 不需要发送结果

            // 通过WebSocket推送撤回通知给接收方
            imClient.sendPrivateMessage(sendMessage);

            // 构建推送给自己其他终端的撤回消息
            privateMessage.setContent("你撤回了一条消息");  // 修改撤回提示文本
            sendMessage.setSendToSelf(true);  // 发送给自己的其他终端
            sendMessage.setReceiveTerminals(Collections.emptyList());  // 发送给所有终端

            // 通过WebSocket推送撤回通知给自己的其他终端
            imClient.sendPrivateMessage(sendMessage);

            // 记录日志
            logger.info("撤回私聊消息，发送id:{},接收id:{}，内容:{}", privateMessage.getSendId(), privateMessage.getRecvId(), privateMessage.getContent());
        });
    }
}
