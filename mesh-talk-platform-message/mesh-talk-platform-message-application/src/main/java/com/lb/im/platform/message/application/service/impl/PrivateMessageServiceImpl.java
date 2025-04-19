package com.lb.im.platform.message.application.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.alibaba.fastjson.JSONObject;
import com.lb.im.common.cache.id.SnowFlakeFactory;
import com.lb.im.common.mq.MessageSenderService;
import com.lb.im.platform.common.exception.IMException;
import com.lb.im.platform.common.model.constants.IMPlatformConstants;
import com.lb.im.platform.common.model.dto.PrivateMessageDTO;
import com.lb.im.platform.common.model.enums.HttpCode;
import com.lb.im.platform.common.session.SessionContext;
import com.lb.im.platform.common.session.UserSession;
import com.lb.im.platform.dubbo.friend.FriendDubboService;
import com.lb.im.platform.message.application.service.PrivateMessageService;
import com.lb.im.platform.message.domain.event.IMPrivateMessageTxEvent;
import com.lb.im.platform.message.domain.service.PrivateMessageDomainService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class PrivateMessageServiceImpl implements PrivateMessageService {

    private final Logger logger = LoggerFactory.getLogger(PrivateMessageServiceImpl.class);

    @DubboReference(version = IMPlatformConstants.DEFAULT_DUBBO_VERSION, check = false)
    private FriendDubboService friendDubboService;

    @Autowired
    private MessageSenderService messageSenderService;
    @Autowired
    private PrivateMessageDomainService privateMessageDomainService;

    @Override
    public Long sendMessage(PrivateMessageDTO dto) {
        UserSession session = SessionContext.getSession();
        Boolean isFriend = friendDubboService.isFriend(session.getUserId(), dto.getRecvId());
        if (BooleanUtil.isFalse(isFriend)) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "对方不是你的好友，无法发送消息");
        }
        Long messageId = SnowFlakeFactory.getSnowFlakeFromCache().nextId();
        //组装事务消息数据
        IMPrivateMessageTxEvent imPrivateMessageTxEvent = new IMPrivateMessageTxEvent(messageId,
                                                                                      session.getUserId(),
                                                                                      session.getTerminal(),
                                                                                      IMPlatformConstants.TOPIC_PRIVATE_TX_MESSAGE,
                                                                                      new Date(),
                                                                                      dto);
        TransactionSendResult sendResult = messageSenderService.sendMessageInTransaction(imPrivateMessageTxEvent, null);
        if (sendResult.getSendStatus() != SendStatus.SEND_OK) {
            logger.error("PrivateMessageServiceImpl|发送事务消息失败|参数:{}", JSONObject.toJSONString(dto));
        }
        return messageId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveIMPrivateMessageSaveEvent(IMPrivateMessageTxEvent privateMessageSaveEvent) {
        return privateMessageDomainService.saveIMPrivateMessageSaveEvent(privateMessageSaveEvent);
    }

    @Override
    public boolean checkExists(Long messageId) {
        return privateMessageDomainService.checkExists(messageId);
    }
}
