package com.lb.im.platform.group.appliication.event;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.event.EventHandler;
import com.alibaba.cola.event.EventHandlerI;
import com.alibaba.fastjson.JSON;
import com.lb.im.platform.group.appliication.cache.GroupCacheService;
import com.lb.im.platform.group.domain.event.IMGroupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@EventHandler
@ConditionalOnProperty(name = "message.mq.event.type", havingValue = "cola")
public class IMGroupColaEventHandler implements EventHandlerI<Response, IMGroupEvent> {

    private final Logger logger = LoggerFactory.getLogger(IMGroupColaEventHandler.class);

    @Autowired
    private GroupCacheService groupCacheService;

    @Override
    public Response execute(IMGroupEvent imGroupEvent) {
        if (imGroupEvent == null || imGroupEvent.getId() == null) {
            logger.info("cola|groupEvent|接收群组事件参数错误");
            return Response.buildSuccess();
        }
        logger.info("cola|groupEvent|接收群组事件参数|{}", JSON.toJSON(imGroupEvent));
        groupCacheService.updateGroupCache(imGroupEvent);
        return Response.buildSuccess();
    }
}
