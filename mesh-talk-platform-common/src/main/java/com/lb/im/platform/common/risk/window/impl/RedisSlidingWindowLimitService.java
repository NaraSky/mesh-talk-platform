package com.lb.im.platform.common.risk.window.impl;

import com.lb.im.platform.common.model.constants.IMPlatformConstants;
import com.lb.im.platform.common.risk.window.SlidingWindowLimitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(name = "distribute.cache.type", havingValue = "redis")
public class RedisSlidingWindowLimitService implements SlidingWindowLimitService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean passThough(String key, long windowPeriod, int windowSize) {
        //风控key
        String riskControlKey = IMPlatformConstants.getKey(IMPlatformConstants.RISK_CONTROL_KEY_PREFIX, key);
        //获取当前时间
        long currentTimeStamp = System.currentTimeMillis();
        long length = windowPeriod * windowSize;
        long start = currentTimeStamp - length;
        //计算过期时间
        long expireTime = length + windowPeriod;
        redisTemplate.opsForZSet().add(riskControlKey, String.valueOf(currentTimeStamp), currentTimeStamp);
        // 移除[0,start]区间内的值
        redisTemplate.opsForZSet().removeRangeByScore(riskControlKey, 0, start);
        // 获取窗口内元素个数
        Long count = redisTemplate.opsForZSet().zCard(riskControlKey);
        // 过期时间 窗口长度+一个时间间隔
        redisTemplate.expire(riskControlKey, expireTime, TimeUnit.MILLISECONDS);
        //count为空不能通过
        if (count == null) {
            return false;
        }
        return count <= windowSize;
    }
}
