package com.lb.im.platform.message.application.consumer;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.lb.im.common.domain.constans.IMConstants;
import com.lb.im.common.domain.model.IMGroupMessage;
import com.lb.im.common.domain.model.IMUserInfo;
import com.lb.im.platform.common.model.constants.IMPlatformConstants;
import com.lb.im.platform.common.model.enums.MessageStatus;
import com.lb.im.platform.common.model.vo.GroupMessageVO;
import com.lb.im.platform.common.utils.BeanUtils;
import com.lb.im.platform.message.domain.event.IMGroupMessageTxEvent;
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
@RocketMQMessageListener(consumerGroup = IMPlatformConstants.TOPIC_GROUP_TX_MESSAGE_GROUP, topic = IMPlatformConstants.TOPIC_GROUP_TX_MESSAGE)
public class IMGroupMessageRocketMQEventConsumer implements RocketMQListener<String> {
    private final Logger logger = LoggerFactory.getLogger(IMGroupMessageRocketMQEventConsumer.class);

    @Autowired
    private IMClient imClient;

    @Override
    public void onMessage(String message) {
        if (StrUtil.isEmpty(message)){
            logger.info("rocketmq|groupMessageTxConsumer|接收消息微服务发送过来的群聊消息事件参数为空" );
            return;
        }
        logger.info("rocketmq|groupMessageTxConsumer|接收消息微服务发送过来的群聊消息事件|{}", message);
        IMGroupMessageTxEvent imGroupMessageTxEvent = this.getEventMessage(message);
        if (imGroupMessageTxEvent == null || imGroupMessageTxEvent.getGroupMessageDTO() == null){
            logger.error("rocketmq|groupMessageTxConsumer|接收消息微服务发送过来的群聊消息事件转换失败");
            return;
        }
        GroupMessageVO groupMessageVO = BeanUtils.copyProperties(imGroupMessageTxEvent.getGroupMessageDTO(), GroupMessageVO.class);
        groupMessageVO.setId(imGroupMessageTxEvent.getId());
        groupMessageVO.setSendId(imGroupMessageTxEvent.getSenderId());
        groupMessageVO.setSendNickName(imGroupMessageTxEvent.getSendNickName());
        groupMessageVO.setSendTime(imGroupMessageTxEvent.getSendTime());
        groupMessageVO.setStatus(MessageStatus.UNSEND.code());
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(imGroupMessageTxEvent.getSenderId(), imGroupMessageTxEvent.getTerminal()));
        sendMessage.setReceiveIds(imGroupMessageTxEvent.getUserIds());
        sendMessage.setData(groupMessageVO);
        imClient.sendGroupMessage(sendMessage);
        logger.info("发送群聊消息，发送者id:{},群组id:{},内容:{}", imGroupMessageTxEvent.getSenderId(), imGroupMessageTxEvent.getGroupMessageDTO().getGroupId(), imGroupMessageTxEvent.getGroupMessageDTO().getContent());
    }

    private IMGroupMessageTxEvent getEventMessage(String msg){
        JSONObject jsonObject = JSONObject.parseObject(msg);
        String eventStr = jsonObject.getString(IMConstants.MSG_KEY);
        return JSONObject.parseObject(eventStr, IMGroupMessageTxEvent.class);
    }
}
