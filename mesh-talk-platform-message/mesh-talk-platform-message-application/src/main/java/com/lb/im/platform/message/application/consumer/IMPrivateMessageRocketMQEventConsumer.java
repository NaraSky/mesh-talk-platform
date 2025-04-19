package com.lb.im.platform.message.application.consumer;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.lb.im.common.domain.constans.IMConstants;
import com.lb.im.common.domain.model.IMPrivateMessage;
import com.lb.im.common.domain.model.IMUserInfo;
import com.lb.im.platform.common.model.constants.IMPlatformConstants;
import com.lb.im.platform.common.model.enums.MessageStatus;
import com.lb.im.platform.common.model.vo.PrivateMessageVO;
import com.lb.im.platform.common.utils.BeanUtils;
import com.lb.im.platform.message.domain.event.IMPrivateMessageTxEvent;
import com.lb.im.sdk.client.IMClient;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "message.mq.type", havingValue = "rocketmq")
@RocketMQMessageListener(consumerGroup = IMPlatformConstants.TOPIC_PRIVATE_TX_MESSAGE_GROUP, topic = IMPlatformConstants.TOPIC_PRIVATE_TX_MESSAGE)
public class IMPrivateMessageRocketMQEventConsumer implements RocketMQListener<String> {
    private final Logger logger = LoggerFactory.getLogger(IMPrivateMessageRocketMQEventConsumer.class);

    @Autowired
    private IMClient imClient;

    @Override
    public void onMessage(String message) {
        if (StrUtil.isEmpty(message)) {
            logger.info("rocketmq|privateMessageTxConsumer|接收消息微服务发送过来的单聊消息事件参数为空");
            return;
        }
        logger.info("rocketmq|privateMessageTxConsumer|接收消息微服务发送过来的单聊消息事件|{}", message);
        IMPrivateMessageTxEvent imPrivateMessageTxEvent = this.getEventMessage(message);
        if (imPrivateMessageTxEvent == null || imPrivateMessageTxEvent.getPrivateMessageDTO() == null) {
            logger.error("rocketmq|privateMessageTxConsumer|接收消息微服务发送过来的单聊消息事件转换失败");
            return;
        }
        PrivateMessageVO privateMessageVO = BeanUtils.copyProperties(imPrivateMessageTxEvent.getPrivateMessageDTO(), PrivateMessageVO.class);
        //设置消息id
        privateMessageVO.setId(imPrivateMessageTxEvent.getId());
        //设置发送者id
        privateMessageVO.setSendId(imPrivateMessageTxEvent.getSenderId());
        //设置状态
        privateMessageVO.setStatus(MessageStatus.UNSEND.code());
        //发送时间
        privateMessageVO.setSendTime(imPrivateMessageTxEvent.getSendTime());
        //封装发送消息数据模型
        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(privateMessageVO.getSendId(), imPrivateMessageTxEvent.getTerminal()));
        sendMessage.setReceiveId(privateMessageVO.getRecvId());
        sendMessage.setSendToSelf(true);
        sendMessage.setData(privateMessageVO);
        imClient.sendPrivateMessage(sendMessage);
        logger.info("发送私聊消息，发送id:{},接收id:{}，内容:{}", privateMessageVO.getSendId(), privateMessageVO.getRecvId(), privateMessageVO.getContent());
    }

    private IMPrivateMessageTxEvent getEventMessage(String msg) {
        JSONObject jsonObject = JSONObject.parseObject(msg);
        String eventStr = jsonObject.getString(IMConstants.MSG_KEY);
        return JSONObject.parseObject(eventStr, IMPrivateMessageTxEvent.class);
    }
}
