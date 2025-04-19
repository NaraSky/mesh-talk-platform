package com.lb.im.platform.group.appliication.consumer;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.lb.im.common.domain.constans.IMConstants;
import com.lb.im.platform.common.model.constants.IMPlatformConstants;
import com.lb.im.platform.common.model.event.User2GroupEvent;
import com.lb.im.platform.group.appliication.service.GroupService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "message.mq.type", havingValue = "rocketmq")
@RocketMQMessageListener(consumerGroup = IMPlatformConstants.TOPIC_USER_TO_GROUP_GROUP, topic = IMPlatformConstants.TOPIC_USER_TO_GROUP)
public class IMGroupRocketMQEventConsumer implements RocketMQListener<String> {
    private final Logger logger = LoggerFactory.getLogger(IMGroupRocketMQEventConsumer.class);

    @Autowired
    private GroupService groupService;

    @Override
    public void onMessage(String message) {
        if (StrUtil.isEmpty(message)) {
            logger.info("rocketmq|groupConsumer|接收群组微服务发送过来的事件参数为空");
            return;
        }
        logger.info("rocketmq|groupConsumer|接收群组微服务发送过来的事件|{}", message);
        User2GroupEvent user2GroupEvent = this.getEventMessage(message);
        groupService.updateHeadImgByUserId(user2GroupEvent.getHeadImageThumb(), user2GroupEvent.getId());
    }

    private User2GroupEvent getEventMessage(String msg) {
        JSONObject jsonObject = JSONObject.parseObject(msg);
        String eventStr = jsonObject.getString(IMConstants.MSG_KEY);
        return JSONObject.parseObject(eventStr, User2GroupEvent.class);
    }
}
