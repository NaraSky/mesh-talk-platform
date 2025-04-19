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
     */
    public static final Long DEFAULT_REDIS_CACHE_EXPIRE_TIME = 2L;

    /**
     * 绑定好友
     */
    public static final String FRIEND_HANDLER_BIND = "bind";

    /**
     * 解除绑定
     */
    public static final String FRIEND_HANDLER_UNBIND = "unbind";

    /**
     * 更新好友信息
     */
    public static final String FRIEND_HANDLER_UPDATE = "update";

    /**
     * 创建群聊
     */
    public static final String GROUP_HANDLER_CREATE = "create";

    /**
     * 修改群聊
     */
    public static final String GROUP_HANDLER_MODIFY = "modify";

    /**
     * 删除群聊
     */
    public static final String GROUP_HANDLER_DELETE = "delete";

    /**
     * 退群
     */
    public static final String GROUP_HANDLER_QUIT = "quit";

    /**
     * 踢人
     */
    public static final String GROUP_HANDLER_KICK = "kick";

    /**
     * 邀请人进群
     */
    public static final String GROUP_HANDLER_INVITE = "invite";

    /**
     * 大后端平台的用户key
     */
    public static final String PLATFORM_REDIS_USER_KEY = "platform:user:";

    /**
     * 大后端平台的好友key
     */
    public static final String PLATFORM_REDIS_FRIEND_SINGLE_KEY = "platform:friend:single:";

    /**
     * 好友列表
     */
    public static final String PLATFORM_REDIS_FRIEND_LIST_KEY = "platform:friend:list:";

    /**
     * 是否是好友关系
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
     * 群成员vo
     */
    public static final String PLATFORM_REDIS_MEMBER_VO_SIMPLE_KEY = "platform:member:simple:vo:";

    /**
     * simple vo list
     */
    public static final String PLATFORM_REDIS_MEMBER_LIST_SIMPLE_KEY = "platform:member:simple:list:";

    /**
     * 群成员id列表
     */
    public static final String PLATFORM_REDIS_MEMBER_ID_KEY = "platform:member:id:list:";

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
     * 群组事件Topic
     */
    public static final String TOPIC_EVENT_ROCKETMQ_GROUP = "topic_event_rocketmq_group";

    /**
     * 好友事件Topic
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
     */
    public static final String TOPIC_EVENT_COLA = "topic_event_cola";

    /**
     * 更新用户分布式缓存时用的锁前缀
     */
    public static final String IM_USER_UPDATE_CACHE_LOCK_KEY = "IM_USER_UPDATE_CACHE_LOCK_KEY_";

    /**
     * 更新好友分布式缓存时用的锁前缀
     */
    public static final String IM_FRIEND_UPDATE_CACHE_LOCK_KEY = "IM_FRIEND_UPDATE_CACHE_LOCK_KEY_";

    /**
     * 用户事件消费分组
     */
    public static final String EVENT_USER_CONSUMER_GROUP = "event_user_consumer_group";

    /**
     * 群组事件消费分组
     */
    public static final String EVENT_GROUP_CONSUMER_GROUP = "event_group_consumer_group";

    /**
     * 好友事件消费分组
     */
    public static final String EVENT_FRIEND_CONSUMER_GROUP = "event_friend_consumer_group";

    /**
     * 默认Dubbo版本
     */
    public static final String DEFAULT_DUBBO_VERSION = "1.0.0";


    public static String getKey(String prefix, String key) {
        return prefix.concat(key);
    }
}
