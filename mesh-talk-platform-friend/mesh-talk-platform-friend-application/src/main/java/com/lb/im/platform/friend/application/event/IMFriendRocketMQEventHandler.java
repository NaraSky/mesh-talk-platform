package com.lb.im.platform.friend.application.event;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.lb.im.common.domain.constans.IMConstants;
import com.lb.im.platform.common.model.constants.IMPlatformConstants;
import com.lb.im.platform.friend.application.cache.FriendCacheService;
import com.lb.im.platform.friend.domain.event.IMFriendEvent;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * RocketMQ好友事件处理器
 * 
 * 技术点：
 * 1. 使用RocketMQ实现事件驱动架构
 * 2. 采用Spring的条件化配置，根据配置决定是否启用
 * 3. 使用RocketMQ的消息监听机制处理异步事件
 * 4. 实现缓存与数据库的最终一致性
 */
@Component
@ConditionalOnProperty(name = "message.mq.event.type", havingValue = "rocketmq") // 只有当配置为rocketmq时才启用此组件
@RocketMQMessageListener(
    consumerGroup = IMPlatformConstants.EVENT_FRIEND_CONSUMER_GROUP, // 消费者组
    topic = IMPlatformConstants.TOPIC_EVENT_ROCKETMQ_FRIEND // 订阅的主题
)
public class IMFriendRocketMQEventHandler implements RocketMQListener<String> {

    private final Logger logger = LoggerFactory.getLogger(IMFriendRocketMQEventHandler.class);

    /**
     * 好友缓存服务
     * 用于更新好友关系的缓存数据
     */
    @Autowired
    private FriendCacheService friendCacheService;

    /**
     * 消息处理方法
     * 接收并处理RocketMQ发送的好友事件消息
     * 
     * @param message RocketMQ消息内容（JSON字符串）
     */
    @Override
    public void onMessage(String message) {
        logger.info("rocketmq|friendEvent|接收好友事件|{}", message);

        // 参数校验
        if (StrUtil.isEmpty(message)){
            logger.info("rocketmq|friendEvent|接收好友事件参数错误" );
            return;
        }

        // 解析事件消息
        IMFriendEvent friendEvent = this.getEventMessage(message);

        // 更新好友缓存
        friendCacheService.updateFriendCache(friendEvent);
    }

    /**
     * 解析事件消息
     * 将JSON字符串转换为IMFriendEvent对象
     * 
     * @param msg JSON格式的消息字符串
     * @return 解析后的好友事件对象
     */
    private IMFriendEvent getEventMessage(String msg){
        // 解析JSON对象
        JSONObject jsonObject = JSONObject.parseObject(msg);

        // 获取事件内容字符串
        String eventStr = jsonObject.getString(IMConstants.MSG_KEY);

        // 将事件内容转换为IMFriendEvent对象
        return JSONObject.parseObject(eventStr, IMFriendEvent.class);
    }
}
