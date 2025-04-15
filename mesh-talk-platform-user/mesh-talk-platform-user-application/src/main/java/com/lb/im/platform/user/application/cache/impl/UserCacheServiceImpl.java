package com.lb.im.platform.user.application.cache.impl;

import com.lb.im.common.cache.distribute.DistributedCacheService;
import com.lb.im.common.cache.lock.DistributedLock;
import com.lb.im.common.cache.lock.factory.DistributedLockFactory;
import com.lb.im.platform.common.model.constants.IMPlatformConstants;
import com.lb.im.platform.common.model.entity.User;
import com.lb.im.platform.user.application.cache.UserCacheService;
import com.lb.im.platform.user.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class UserCacheServiceImpl implements UserCacheService {

    private final Logger logger = LoggerFactory.getLogger(UserCacheServiceImpl.class);
    @Autowired
    private DistributedCacheService distributedCacheService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DistributedLockFactory distributedLockFactory;

    @Override
    public void updateUserCache(Long userId) {
        if (userId == null) {
            logger.info("IMUserCache|更新分布式缓存时，用户id为空");
            return;
        }
        logger.info("IMUserCache|更新分布式缓存|{}", userId);
        //获取分布式锁，保证只有一个线程在更新分布式缓存
        DistributedLock lock = distributedLockFactory.getDistributedLock(IMPlatformConstants.IM_USER_UPDATE_CACHE_LOCK_KEY.concat(String.valueOf(userId)));
        try {
            boolean isSuccess = lock.tryLock();
            if (!isSuccess) {
                return;
            }
            //只有获取到锁的线程才会向下执行
            User user = userRepository.selectById(userId);
            if (user == null) {
                return;
            }

            //更新用户id的缓存数据
            String userIdKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_USER_KEY, userId);
            distributedCacheService.set(userIdKey, user, IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME, TimeUnit.MINUTES);

            //更新用户名的缓存数据
            String userNameKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_USER_KEY, user.getUserName());
            distributedCacheService.set(userNameKey, user, IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        } catch (Exception e) {
            logger.error("IMUserCache|更新分布式缓存失败|{}", userId);
        } finally {
            lock.unlock();
        }
    }
}
