package com.lb.im.platform.common.model.constants;

/**
 * IM平台常量类
 * 技术点：
 * 1. 使用常量类集中管理系统中的常量值，提高代码可维护性
 * 2. 为Redis缓存、消息队列等提供统一的键名和配置
 * 3. 支持分布式系统中的各种标识符和配置项
 */
public class IMPlatformConstants {

    /**
     * 缓存数据默认10分钟过期
     * 用于设置Redis缓存的默认过期时间
     */
    public static final Long DEFAULT_REDIS_CACHE_EXPIRE_TIME = 2L;

    /**
     * 好友关系操作类型 - 添加好友
     */
    public static final String FRIEND_HANDLER_BIND = "bind";

    /**
     * 好友关系操作类型 - 删除好友
     */
    public static final String FRIEND_HANDLER_UNBIND = "unbind";

    /**
     * 更新好友信息
     * 好友关系操作类型 - 更新好友资料
     */
    public static final String FRIEND_HANDLER_UPDATE = "update";

    /**
     * 大后端平台的用户key
     * Redis中存储用户信息的键前缀
     */
    public static final String PLATFORM_REDIS_USER_KEY = "platform:user:";

    /**
     * 大后端平台的好友key
     * Redis中存储单个好友关系的键前缀
     */
    public static final String PLATFORM_REDIS_FRIEND_SINGLE_KEY = "platform:friend:single:";

    /**
     * 好友列表
     * Redis中存储用户好友列表的键前缀
     */
    public static final String PLATFORM_REDIS_FRIEND_LIST_KEY = "platform:friend:list:";

    /**
     * 是否是好友关系
     * Redis中存储好友关系集合的键前缀，用于快速判断是否为好友
     */
    public static final String PLATFORM_REDIS_FRIEND_SET_KEY = "platform:friend:set:";

    /**
     * 群组列表
     */
    public static final String PLATFORM_REDIS_GROUP_LIST_KEY = "platform:group:list:";

    /**
     * 单个群组
     */
    public static final String PLATFORM_REDIS_GROUP_SINGLE_KEY = "platform:group:single:";

    /**
     * 单个群组vo
     */
    public static final String PLATFORM_REDIS_GROUP_VO_SINGLE_KEY = "platform:group:vo:single:";

    /**
     * 群成员列表
     */
    public static final String PLATFORM_REDIS_MEMBER_VO_LIST_KEY = "platform:member:vo:list:";

    /**
     * Session数据
     * 用户会话数据的键名
     */
    public static final String SESSION = "session";

    /**
     * 风控前缀
     * 风险控制相关的Redis键前缀
     */
    public static final String RISK_CONTROL_KEY_PREFIX = "risk:control:";

    /**
     * AccessToken
     * 用户访问令牌的键名
     */
    public static final String ACCESS_TOKEN = "accessToken";

    /**
     * cola事件类型
     * 使用COLA框架的事件发布类型标识
     */
    public static final String EVENT_PUBLISH_TYPE_COLA = "cola";

    /**
     * RocketMQ事件类型
     * 使用RocketMQ的事件发布类型标识
     */
    public static final String EVENT_PUBLISH_TYPE_ROCKETMQ = "rocketmq";

    /**
     * 用户事件Topic
     * RocketMQ中用户相关事件的主题名称
     */
    public static final String TOPIC_EVENT_ROCKETMQ_USER = "topic_event_rocketmq_user";

    /**
     * 好友事件Topic
     * RocketMQ中好友相关事件的主题名称
     */
    public static final String TOPIC_EVENT_ROCKETMQ_FRIEND = "topic_event_rocketmq_friend";

    /**
     * 更新用户信息异步更新好友表数据
     */
    public static final String TOPIC_USER_TO_FRIEND = "topic_user_to_friend";

    /**
     * 消费者分组
     */
    public static final String TOPIC_USER_TO_FRIEND_GROUP = "topic_user_to_friend_group";


    /**
     * Cola订阅事件
     * COLA框架中事件的主题名称
     */
    public static final String TOPIC_EVENT_COLA = "topic_event_cola";

    /**
     * 更新用户分布式缓存时用的锁前缀
     * 用于分布式锁，确保同一时间只有一个线程更新用户缓存
     */
    public static final String IM_USER_UPDATE_CACHE_LOCK_KEY = "IM_USER_UPDATE_CACHE_LOCK_KEY_";

    /**
     * 更新好友分布式缓存时用的锁前缀
     * 用于分布式锁，确保同一时间只有一个线程更新好友缓存
     */
    public static final String IM_FRIEND_UPDATE_CACHE_LOCK_KEY = "IM_FRIEND_UPDATE_CACHE_LOCK_KEY_";

    /**
     * 用户事件消费分组
     * RocketMQ中消费用户事件的消费者组名称
     */
    public static final String EVENT_USER_CONSUMER_GROUP = "event_user_consumer_group";

    /**
     * 好友事件消费分组
     * RocketMQ中消费好友事件的消费者组名称
     */
    public static final String EVENT_FRIEND_CONSUMER_GROUP = "event_friend_consumer_group";

    /**
     * 默认Dubbo版本
     * Dubbo服务的默认版本号
     */
    public static final String DEFAULT_DUBBO_VERSION = "1.0.0";

    /**
     * 拼接Redis键
     * 将前缀和键名拼接成完整的Redis键
     * 
     * @param prefix 键前缀
     * @param key 键名
     * @return 完整的Redis键
     */
    public static String getKey(String prefix, String key) {
        return prefix.concat(key);
    }
}
