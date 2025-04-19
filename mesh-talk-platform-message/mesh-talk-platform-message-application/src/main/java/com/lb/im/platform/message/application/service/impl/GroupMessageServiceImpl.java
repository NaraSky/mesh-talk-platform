package com.lb.im.platform.message.application.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.lb.im.common.cache.distribute.DistributedCacheService;
import com.lb.im.common.cache.id.SnowFlakeFactory;
import com.lb.im.common.domain.constans.IMConstants;
import com.lb.im.common.domain.model.IMGroupMessage;
import com.lb.im.common.domain.model.IMUserInfo;
import com.lb.im.common.mq.MessageSenderService;
import com.lb.im.platform.common.exception.IMException;
import com.lb.im.platform.common.model.constants.IMPlatformConstants;
import com.lb.im.platform.common.model.dto.GroupMessageDTO;
import com.lb.im.platform.common.model.enums.HttpCode;
import com.lb.im.platform.common.model.enums.MessageStatus;
import com.lb.im.platform.common.model.params.GroupParams;
import com.lb.im.platform.common.model.vo.GroupMemberSimpleVO;
import com.lb.im.platform.common.model.vo.GroupMessageVO;
import com.lb.im.platform.common.session.SessionContext;
import com.lb.im.platform.common.session.UserSession;
import com.lb.im.platform.common.threadpool.GroupMessageThreadPoolUtils;
import com.lb.im.platform.common.utils.DateTimeUtils;
import com.lb.im.platform.dubbo.group.GroupDubboService;
import com.lb.im.platform.message.application.service.GroupMessageService;
import com.lb.im.platform.message.domain.event.IMGroupMessageTxEvent;
import com.lb.im.platform.message.domain.service.GroupMessageDomainService;
import com.lb.im.sdk.client.IMClient;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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

    // 分布式缓存服务，用于存储和获取消息读取位置等信息
    @Autowired
    private DistributedCacheService distributedCacheService;

    // IM客户端，用于向用户推送消息
    @Autowired
    private IMClient imClient;

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
        GroupMemberSimpleVO groupMemberSimpleVO = groupDubboService.getGroupMemberSimpleVO(new GroupParams(userSession.getUserId(), dto.getGroupId()));

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

    /**
     * 异步拉取未读群聊消息
     * 获取当前用户所在的所有群组，并拉取每个群组中的未读消息
     * 通过WebSocket异步推送给用户当前终端
     * 
     * 实现步骤：
     * 1. 获取当前用户所在的所有群组
     * 2. 对每个群组并行处理，获取用户在该群组的最后读取位置
     * 3. 查询该位置之后的所有未读消息
     * 4. 使用线程池异步将消息推送给用户当前终端
     */
    @Override
    public void pullUnreadMessage() {
        // 获取当前用户会话
        UserSession session = SessionContext.getSession();
        // 获取用户所在的所有群组
        List<GroupMemberSimpleVO> groupMemberList = groupDubboService.getGroupMemberSimpleVOList(session.getUserId());
        if (CollectionUtil.isEmpty(groupMemberList)) {
            return;
        }
        groupMemberList.parallelStream().forEach((member) -> {
            String key = String.join(IMConstants.REDIS_KEY_SPLIT, IMConstants.IM_GROUP_READED_POSITION, member.getGroupId().toString(), session.getUserId().toString());
            String maxReadedIdStr = distributedCacheService.get(key);
            Long maxReadedId = StrUtil.isEmpty(maxReadedIdStr) ? 0L : Long.parseLong(maxReadedIdStr);
            List<GroupMessageVO> unreadGroupMessageList = groupMessageDomainService.getUnreadGroupMessageList(member.getGroupId(), member.getCreatedTime(),
                                                                                                              session.getUserId(), MessageStatus.RECALL.code(), maxReadedId, IMPlatformConstants.PULL_HISTORY_MESSAGE_LIMIT_COUNR);
            if (!CollectionUtil.isEmpty(unreadGroupMessageList)) {
                GroupMessageThreadPoolUtils.execute(() -> {
                    for (GroupMessageVO message : unreadGroupMessageList) {
                        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
                        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
                        // 只推给自己当前终端
                        sendMessage.setReceiveIds(Collections.singletonList(session.getUserId()));
                        sendMessage.setReceiveTerminals(Collections.singletonList(session.getTerminal()));
                        sendMessage.setData(message);
                        imClient.sendGroupMessage(sendMessage);
                    }
                });
                // 发送消息
                logger.info("拉取未读群聊消息，用户id:{},群聊id:{},数量:{}", session.getUserId(), member.getGroupId(), unreadGroupMessageList.size());
            }
        });
    }

    /**
     * 拉取群聊消息
     * 加载指定ID之后的群聊消息，只能拉取最近1个月的消息，一次最多拉取100条
     * 
     * @param minId 消息的最小ID，拉取该ID之后的消息
     * @return 群聊消息列表，按时间顺序排序
     */
    @Override
    public List<GroupMessageVO> loadMessage(Long minId) {
        // 获取当前用户会话
        UserSession session = SessionContext.getSession();
        // 获取用户所在的所有群组ID
        List<Long> ids = groupDubboService.getUserIdsByGroupId(session.getUserId());
        if (CollectionUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        // 只能拉取最近1个月的
        Date minDate = DateTimeUtils.addMonths(new Date(), -1);
        List<GroupMessageVO> groupMessageList = groupMessageDomainService.loadGroupMessageList(minId, minDate, ids,
                                                                                               MessageStatus.RECALL.code(), IMPlatformConstants.PULL_HISTORY_MESSAGE_LIMIT_COUNR);
        if (CollectionUtil.isEmpty(groupMessageList)) {
            return Collections.emptyList();
        }
        List<GroupMessageVO> vos = groupMessageList.stream().peek(m -> {
            // 被@用户列表
            List<String> atIds = Arrays.asList(StrUtil.split(m.getAtUserIdsStr(), IMConstants.USER_ID_SPLIT));
            m.setAtUserIds(atIds.stream().map(Long::parseLong).collect(Collectors.toList()));
        }).collect(Collectors.toList());
        // 消息状态,数据库没有存群聊的消息状态，需要从redis取
        List<String> keys = ids.stream().map(id -> String.join(IMConstants.REDIS_KEY_SPLIT, IMConstants.IM_GROUP_READED_POSITION,
                                                               id.toString(), session.getUserId().toString())).collect(Collectors.toList());
        List<String> sendPos = distributedCacheService.multiGet(keys);
        for (int idx = 0; idx < ids.size(); idx++) {
            Long id = ids.get(idx);
            String str = sendPos.get(idx);
            Long sendMaxId = StrUtil.isEmpty(str) ? 0L : Long.parseLong(str);
            vos.stream().filter(vo -> vo.getGroupId().equals(id)).forEach(vo -> {
                if (vo.getId() <= sendMaxId) {
                    // 已读
                    vo.setStatus(MessageStatus.READED.code());
                } else {
                    // 未推送
                    vo.setStatus(MessageStatus.UNSEND.code());
                }
            });
        }
        return vos;
    }

    /**
     * 查询群聊历史消息
     * 分页获取指定群组的历史聊天记录
     * 
     * @param groupId 群组ID
     * @param page 页码，从1开始，如果小于1则使用默认值1
     * @param size 每页大小，如果小于1则使用默认值10
     * @return 群聊历史消息列表，按时间倒序排序
     * @throws IMException 当用户不在群组中时抛出异常
     */
    @Override
    public List<GroupMessageVO> findHistoryMessage(Long groupId, Long page, Long size) {
        // 参数校验和默认值设置
        page = page > 0 ? page : IMPlatformConstants.DEFAULT_PAGE;
        size = size > 0 ? size : IMPlatformConstants.DEFAULT_PAGE_SIZE;
        // 获取当前用户ID
        Long userId = SessionContext.getSession().getUserId();
        // 计算分页起始索引
        long stIdx = (page - 1) * size;
        GroupMemberSimpleVO groupMember = groupDubboService.getGroupMemberSimpleVO(new GroupParams(userId, groupId));
        if (groupMember == null || groupMember.getQuit()) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "您已不在群聊中");
        }
        List<GroupMessageVO> historyMessage = groupMessageDomainService.getHistoryMessage(groupId, groupMember.getCreatedTime(), MessageStatus.RECALL.code(), stIdx, size);
        if (CollectionUtil.isEmpty(historyMessage)) {
            historyMessage = Collections.emptyList();
        }
        logger.info("拉取群聊记录，用户id:{},群聊id:{}，数量:{}", userId, groupId, historyMessage.size());
        return historyMessage;
    }
}
