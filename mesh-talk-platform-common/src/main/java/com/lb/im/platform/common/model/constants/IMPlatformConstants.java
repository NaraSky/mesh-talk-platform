package com.lb.im.platform.common.model.constants;

public class IMPlatformConstants {

    /**
     * 缓存数据默认10分钟过期
     */
    public static final Long DEFAULT_REDIS_CACHE_EXPIRE_TIME = 10L;

    /**
     * 大后端平台的用户key
     */
    public static final String PLATFORM_REDIS_USER_KEY = "platform:user:";

    /**
     * Session数据
     */
    public static final String SESSION = "session";

    /**
     * 风控前缀
     */
    public static final String RISK_CONTROL_KEY_PREFIX = "risk:control:";

    /**
     * AccessToken
     */
    public static final String ACCESS_TOKEN = "accessToken";

    /**
     * cola事件类型
     */
    public static final String EVENT_PUBLISH_TYPE_COLA = "cola";

    /**
     * RocketMQ事件类型
     */
    public static final String EVENT_PUBLISH_TYPE_ROCKETMQ = "rocketmq";

    /**
     * 用户事件Topic
     */
    public static final String TOPIC_EVENT_ROCKETMQ_USER = "topic_event_rocketmq_user";

    /**
     * Cola订阅事件
     */
    public static final String TOPIC_EVENT_COLA = "topic_event_cola";

    /**
     * 更新用户分布式缓存时用的锁前缀
     */
    public static final String IM_USER_UPDATE_CACHE_LOCK_KEY = "IM_USER_UPDATE_CACHE_LOCK_KEY_";

    /**
     * 用户事件消费分组
     */
    public static final String EVENT_USER_CONSUMER_GROUP = "event_user_consumer_group";

    /**
     * 默认Dubbo版本
     */
    public static final String DEFAULT_DUBBO_VERSION = "1.0.0";

    public static String getKey(String prefix, String key) {
        return prefix.concat(key);
    }
}
