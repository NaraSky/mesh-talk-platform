package com.lb.im.platform.user.application.event;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.lb.im.common.domain.constans.IMConstants;
import com.lb.im.platform.common.model.constants.IMPlatformConstants;
import com.lb.im.platform.user.application.cache.UserCacheService;
import com.lb.im.platform.user.domain.event.IMUserEvent;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * RocketMQ用户事件处理器，负责接收并处理用户相关的事件消息。
 * 通过{@link RocketMQMessageListener}注解配置消费者组和主题，
 * 在message.mq.event.type配置为rocketmq时启用。
 */
@Component
@ConditionalOnProperty(name = "message.mq.event.type", havingValue = "rocketmq")
@RocketMQMessageListener(consumerGroup = IMPlatformConstants.EVENT_USER_CONSUMER_GROUP, topic = IMPlatformConstants.TOPIC_EVENT_ROCKETMQ_USER)
public class IMUserRocketMQEventHandler implements RocketMQListener<String> {

    private final Logger logger = LoggerFactory.getLogger(IMUserRocketMQEventHandler.class);

    @Autowired
    private UserCacheService userCacheService;

    @Override
    public void onMessage(String message) {
        logger.info("rocketmq|userEvent|接收用户事件|{}", message);
        // 校验消息参数有效性
        if (StrUtil.isEmpty(message)){
            logger.info("rocketmq|userEvent|接收用户事件参数错误" );
            return;
        }
        IMUserEvent userEvent = this.getEventMessage(message);
        userCacheService.updateUserCache(userEvent.getId());
    }

    /**
     * 解析消息字符串为IMUserEvent对象。
     * @param msg 消息内容字符串
     * @return 解析后的用户事件对象
     */
    private IMUserEvent getEventMessage(String msg){
        JSONObject jsonObject = JSONObject.parseObject(msg);
        String eventStr = jsonObject.getString(IMConstants.MSG_KEY);
        return JSONObject.parseObject(eventStr, IMUserEvent.class);
    }
}
