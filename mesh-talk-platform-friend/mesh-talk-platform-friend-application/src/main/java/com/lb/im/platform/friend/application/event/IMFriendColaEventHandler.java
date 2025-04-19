package com.lb.im.platform.friend.application.event;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.event.EventHandler;
import com.alibaba.cola.event.EventHandlerI;
import com.alibaba.fastjson.JSON;
import com.lb.im.platform.friend.application.cache.FriendCacheService;
import com.lb.im.platform.friend.domain.event.IMFriendEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * COLA框架好友事件处理器
 * 
 * 技术点：
 * 1. 使用阿里COLA框架实现事件驱动架构
 * 2. 采用Spring的条件化配置，根据配置决定是否启用
 * 3. 实现COLA的EventHandlerI接口处理事件
 * 4. 与RocketMQ事件处理器形成策略模式，根据配置选择不同实现
 */
@EventHandler // COLA框架的事件处理器注解
@ConditionalOnProperty(name = "message.mq.event.type", havingValue = "cola") // 只有当配置为cola时才启用此组件
public class IMFriendColaEventHandler implements EventHandlerI<Response, IMFriendEvent> {

    private final Logger logger = LoggerFactory.getLogger(IMFriendColaEventHandler.class);

    /**
     * 用于更新好友关系的缓存数据
     */
    @Autowired
    private FriendCacheService friendCacheService;

    /**
     * 事件处理方法
     * 接收并处理COLA框架发送的好友事件
     * 
     * @param imFriendEvent 好友事件对象
     * @return COLA框架的响应对象
     */
    @Override
    public Response execute(IMFriendEvent imFriendEvent) {
        if (imFriendEvent == null || imFriendEvent.getId() == null) {
            logger.info("cola|friendEvent|接收好友事件参数错误");
            return Response.buildSuccess();
        }

        logger.info("cola|friendEvent|接收好友事件|{}", JSON.toJSON(imFriendEvent));

        // 更新好友缓存
        friendCacheService.updateFriendCache(imFriendEvent);
        return Response.buildSuccess();
    }
}
