package com.lb.im.platform.friend.application.cache.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.lb.im.common.cache.distribute.DistributedCacheService;
import com.lb.im.common.cache.lock.DistributedLock;
import com.lb.im.common.cache.lock.factory.DistributedLockFactory;
import com.lb.im.platform.common.model.constants.IMPlatformConstants;
import com.lb.im.platform.common.model.entity.Friend;
import com.lb.im.platform.common.model.vo.FriendVO;
import com.lb.im.platform.friend.application.cache.FriendCacheService;
import com.lb.im.platform.friend.domain.event.IMFriendEvent;
import com.lb.im.platform.friend.domain.model.command.FriendCommand;
import com.lb.im.platform.friend.domain.service.FriendDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 好友缓存服务实现类
 * 
 * 技术点：
 * 1. 使用分布式缓存（Redis）管理好友关系数据
 * 2. 采用分布式锁确保缓存更新的线程安全
 * 3. 实现缓存与数据库的最终一致性
 * 4. 根据不同的好友操作类型（添加、删除、更新）采用不同的缓存更新策略
 * 5. 使用策略模式处理不同类型的事件
 */
@Service
public class FriendCacheServiceImpl implements FriendCacheService {
    /**
     * 日志记录器
     * 用于记录缓存操作过程中的日志
     */
    private final Logger logger = LoggerFactory.getLogger(FriendCacheServiceImpl.class);

    /**
     * 分布式缓存服务
     * 用于操作Redis缓存
     */
    @Autowired
    private DistributedCacheService distributedCacheService;

    /**
     * 好友领域服务
     * 用于获取好友关系的数据库数据
     */
    @Autowired
    private FriendDomainService domainService;

    /**
     * 分布式锁工厂
     * 用于创建分布式锁，确保缓存更新的线程安全
     */
    @Autowired
    private DistributedLockFactory distributedLockFactory;

    /**
     * 更新好友缓存
     * 根据好友事件类型更新相应的缓存数据
     * 使用分布式锁确保同一时间只有一个线程在更新同一用户的缓存
     *
     * @param friendEvent 好友事件对象
     */
    @Override
    public void updateFriendCache(IMFriendEvent friendEvent) {
        // 参数校验
        if (friendEvent == null || friendEvent.getId() == null){
            logger.info("IMFriendCacheService|更新分布式缓存时，参数为空");
            return;
        }

        // 获取分布式锁，保证只有一个线程在更新分布式缓存
        // 锁的key使用用户ID确保针对同一用户的操作串行化
        DistributedLock lock = distributedLockFactory.getDistributedLock(
            IMPlatformConstants.IM_FRIEND_UPDATE_CACHE_LOCK_KEY.concat(String.valueOf(friendEvent.getId()))
        );

        try {
            // 尝试获取锁，如果获取失败则直接返回
            boolean isSuccess = lock.tryLock();
            if (!isSuccess){
                return;
            }

            // 根据事件类型选择不同的缓存更新策略
            switch (friendEvent.getHandler()){
                // 添加好友
                case IMPlatformConstants.FRIEND_HANDLER_BIND:
                    this.bindFrind(friendEvent);
                    break;
                // 删除好友
                case IMPlatformConstants.FRIEND_HANDLER_UNBIND:
                    this.unbindFriend(friendEvent);
                    break;
                // 更新好友信息
                case IMPlatformConstants.FRIEND_HANDLER_UPDATE:
                    this.updateFriend(friendEvent);
                    break;
                // 默认处理方式
                default:
                    this.updateFriend(friendEvent);
            }
        } catch (Exception e){
            // 记录异常日志
            logger.error("IMUserCache|更新分布式缓存失败|{}", JSONObject.toJSONString(friendEvent), e);
        } finally {
            // 确保锁被释放
            lock.unlock();
        }
    }

    /**
     * 更新好友信息缓存
     * 当好友信息变更时，更新相关缓存
     *
     * @param friendEvent 好友事件对象
     */
    private void updateFriend(IMFriendEvent friendEvent) {
        String redisKey = "";

        // 更新好友列表缓存
        List<Friend> friendList = domainService.getFriendByUserId(friendEvent.getId());
        if (!CollectionUtil.isEmpty(friendList)){
            redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_FRIEND_LIST_KEY, friendEvent.getId());
            distributedCacheService.set(redisKey, friendList, IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        }

        // 更新单个好友关系缓存
        FriendCommand friendCommand = new FriendCommand(friendEvent.getId(), friendEvent.getFriendId());
        FriendVO friendVO = domainService.findFriend(friendCommand);
        if (friendVO != null){
            redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_FRIEND_SINGLE_KEY, friendCommand);
            distributedCacheService.set(redisKey, friendVO, IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        }
    }

    /**
     * 解除好友关系缓存
     * 当删除好友关系时，更新相关缓存
     *
     * @param friendEvent 好友事件对象
     */
    private void unbindFriend(IMFriendEvent friendEvent) {
        String redisKey = "";

        // 从好友集合中移除
        if (friendEvent.getFriendId() != null){
            redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_FRIEND_SET_KEY, friendEvent.getId());
            distributedCacheService.removeSet(redisKey, String.valueOf(friendEvent.getFriendId()));
        }

        // 更新好友列表缓存
        List<Friend> friendList = domainService.getFriendByUserId(friendEvent.getId());
        redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_FRIEND_LIST_KEY, friendEvent.getId());
        if (!CollectionUtil.isEmpty(friendList)){
            // 如果还有好友，更新列表
            distributedCacheService.set(redisKey, friendList, IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        } else {
            // 如果没有好友了，删除列表缓存
            distributedCacheService.delete(redisKey);
        }

        // 删除单个好友关系缓存
        FriendCommand friendCommand = new FriendCommand(friendEvent.getId(), friendEvent.getFriendId());
        redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_FRIEND_SINGLE_KEY, friendCommand);
        distributedCacheService.delete(redisKey);
    }

    /**
     * 建立好友关系缓存
     * 当添加好友关系时，更新相关缓存
     *
     * @param friendEvent 好友事件对象
     */
    private void bindFrind(IMFriendEvent friendEvent) {
        String redisKey = "";

        // 添加到好友集合中
        if (friendEvent.getFriendId() != null){
            redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_FRIEND_SET_KEY, friendEvent.getId());
            distributedCacheService.addSet(redisKey, String.valueOf(friendEvent.getFriendId()));
        }

        // 更新好友列表缓存
        List<Friend> friendList = domainService.getFriendByUserId(friendEvent.getId());
        if (!CollectionUtil.isEmpty(friendList)){
            redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_FRIEND_LIST_KEY, friendEvent.getId());
            distributedCacheService.set(redisKey, friendList, IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        }

        // 更新单个好友关系缓存
        FriendCommand friendCommand = new FriendCommand(friendEvent.getId(), friendEvent.getFriendId());
        FriendVO friendVO = domainService.findFriend(friendCommand);
        if (friendVO != null){
            redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_FRIEND_SINGLE_KEY, friendCommand);
            distributedCacheService.set(redisKey, friendVO, IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        }
    }
}
