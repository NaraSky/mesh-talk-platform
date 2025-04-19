package com.lb.im.platform.group.appliication.cache.impl;

import com.alibaba.fastjson.JSONObject;
import com.lb.im.common.cache.distribute.DistributedCacheService;
import com.lb.im.platform.common.model.constants.IMPlatformConstants;
import com.lb.im.platform.group.appliication.cache.GroupCacheService;
import com.lb.im.platform.group.domain.command.GroupParams;
import com.lb.im.platform.group.domain.event.IMGroupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 群组缓存服务实现类
 * 负责处理群组相关事件并更新Redis缓存
 */
@Service
public class GroupCacheServiceImpl implements GroupCacheService {
    private final Logger logger = LoggerFactory.getLogger(GroupCacheServiceImpl.class);

    @Autowired
    private DistributedCacheService distributedCacheService; // 分布式缓存服务

    /**
     * 根据群组事件类型更新相应的缓存
     * 
     * @param imGroupEvent 群组事件对象
     */
    @Override
    public void updateGroupCache(IMGroupEvent imGroupEvent) {
        // 当前实现是清除相应缓存，实际生产环境应该优化为更新缓存而非简单删除
        if (imGroupEvent == null) {
            return;
        }
        // 根据事件类型分发到不同的处理方法
        switch (imGroupEvent.getHandler()) {
            case IMPlatformConstants.GROUP_HANDLER_CREATE:
                this.handlerCreate(imGroupEvent);
                break;
            case IMPlatformConstants.GROUP_HANDLER_MODIFY:
                this.handlerModify(imGroupEvent);
                break;
            case IMPlatformConstants.GROUP_HANDLER_DELETE:
                this.handlerDelete(imGroupEvent);
                break;
            case IMPlatformConstants.GROUP_HANDLER_QUIT:
                this.handlerQuit(imGroupEvent);
                break;
            case IMPlatformConstants.GROUP_HANDLER_KICK:
                this.handlerKick(imGroupEvent);
            case IMPlatformConstants.GROUP_HANDLER_INVITE:
                this.handlerInvite(imGroupEvent);
            default:
                logger.info("groupCacheService|群组缓存服务接收到的事件参数为|{}", JSONObject.toJSONString(imGroupEvent));
        }
    }

    /**
     * 处理邀请用户进群事件
     * 
     * @param imGroupEvent 群组事件对象
     */
    private void handlerInvite(IMGroupEvent imGroupEvent) {
        logger.info("groupCacheService|进入邀请事件处理|{}", JSONObject.toJSONString(imGroupEvent));
        this.handlerGroupMember(imGroupEvent);
    }

    /**
     * 处理踢人事件
     * 
     * @param imGroupEvent 群组事件对象
     */
    private void handlerKick(IMGroupEvent imGroupEvent) {
        logger.info("groupCacheService|进入踢人事件处理|{}", JSONObject.toJSONString(imGroupEvent));
        this.handlerGroupMember(imGroupEvent);
    }

    /**
     * 处理退群事件
     * 
     * @param imGroupEvent 群组事件对象
     */
    private void handlerQuit(IMGroupEvent imGroupEvent) {
        logger.info("groupCacheService|进入退群事件处理|{}", JSONObject.toJSONString(imGroupEvent));
        this.handlerGroupMember(imGroupEvent);
    }

    /**
     * 处理删除群组事件
     * 清除群组相关的所有缓存
     * 
     * @param imGroupEvent 群组事件对象
     */
    private void handlerDelete(IMGroupEvent imGroupEvent) {
        logger.info("groupCacheService|进入解散群事件处理|{}", JSONObject.toJSONString(imGroupEvent));
        // 删除群组VO缓存
        String redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_GROUP_VO_SINGLE_KEY, 
                          new GroupParams(imGroupEvent.getUserId(), imGroupEvent.getId()));
        distributedCacheService.delete(redisKey);

        // 删除用户的群组列表缓存
        redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_GROUP_LIST_KEY, 
                   imGroupEvent.getUserId());
        distributedCacheService.delete(redisKey);

        // 删除群组基本信息缓存
        redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_GROUP_SINGLE_KEY, 
                   imGroupEvent.getId());
        distributedCacheService.delete(redisKey);
        // 群成员缓存会自动过期
    }

    /**
     * 处理修改群组事件
     * 清除群组相关的视图缓存
     * 
     * @param imGroupEvent 群组事件对象
     */
    private void handlerModify(IMGroupEvent imGroupEvent) {
        logger.info("groupCacheService|进入修改群事件处理|{}", JSONObject.toJSONString(imGroupEvent));
        // 删除群组VO缓存
        String redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_GROUP_VO_SINGLE_KEY, 
                          new GroupParams(imGroupEvent.getUserId(), imGroupEvent.getId()));
        distributedCacheService.delete(redisKey);

        // 删除群组基本信息缓存
        redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_GROUP_SINGLE_KEY, 
                   imGroupEvent.getId());
        distributedCacheService.delete(redisKey);
    }

    /**
     * 处理创建群组事件
     * 当前仅记录日志，因为创建操作不需要清除缓存
     * 
     * @param imGroupEvent 群组事件对象
     */
    private void handlerCreate(IMGroupEvent imGroupEvent) {
        logger.info("groupCacheService|进入保存群组事件处理|{}", JSONObject.toJSONString(imGroupEvent));
        // 创建群组时不需要清除缓存
    }

    /**
     * 处理群成员变更相关的事件
     * 清除与群成员相关的所有缓存
     * 
     * @param imGroupEvent 群组事件对象
     */
    private void handlerGroupMember(IMGroupEvent imGroupEvent) {
        // 删除群成员列表缓存
        String redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_MEMBER_VO_LIST_KEY, 
                          imGroupEvent.getId());
        distributedCacheService.delete(redisKey);

        // 删除单个成员视图缓存
        redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_MEMBER_VO_SIMPLE_KEY, 
                   new GroupParams(imGroupEvent.getUserId(), imGroupEvent.getId()));
        distributedCacheService.delete(redisKey);

        // 删除群成员ID列表缓存
        redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_MEMBER_ID_KEY, 
                   imGroupEvent.getId());
        distributedCacheService.delete(redisKey);

        // 删除成员简单列表缓存
        redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_MEMBER_LIST_SIMPLE_KEY, 
                   imGroupEvent.getUserId());
        distributedCacheService.delete(redisKey);

        // 删除用户的群组列表缓存
        redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_GROUP_LIST_KEY, 
                   imGroupEvent.getUserId());
        distributedCacheService.delete(redisKey);
    }
}