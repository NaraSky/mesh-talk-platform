package com.lb.im.platform.user.application.event;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.event.EventHandler;
import com.alibaba.cola.event.EventHandlerI;
import com.alibaba.fastjson.JSON;
import com.lb.im.platform.user.application.cache.UserCacheService;
import com.lb.im.platform.user.domain.event.IMUserEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * 处理IM用户事件的COLA事件处理器。
 * 仅在配置属性message.mq.event.type设置为"cola"时生效。
 * 实现EventHandlerI接口，处理IMUserEvent事件并返回响应。
 */
@EventHandler
@ConditionalOnProperty(name = "message.mq.event.type", havingValue = "cola")
public class IMUserColaEventHandler implements EventHandlerI<Response, IMUserEvent> {

    private final Logger logger = LoggerFactory.getLogger(IMUserColaEventHandler.class);

    @Autowired
    private UserCacheService userCacheService;

    /**
     * 处理IM用户事件。
     * @param imUserEvent 用户事件对象，包含用户ID等信息
     * @return 处理结果，成功响应
     */
    @Override
    public Response execute(IMUserEvent imUserEvent) {
        // 校验事件对象及用户ID是否有效，若无效则记录日志并返回成功
        if (imUserEvent == null || imUserEvent.getId() == null){
            logger.info("cola|userEvent|接收用户事件参数错误");
            return Response.buildSuccess();
        }
        // 记录接收到的用户事件详细信息
        logger.info("cola|userEvent|接收用户事件|{}", JSON.toJSON(imUserEvent));
        // 根据用户ID更新用户缓存
        userCacheService.updateUserCache(imUserEvent.getId());
        return Response.buildSuccess();
    }
}
