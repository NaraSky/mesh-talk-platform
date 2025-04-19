package com.lb.im.platform.friend.application.cache;

import com.lb.im.platform.friend.domain.event.IMFriendEvent;

/**
 * 好友缓存服务接口
 * 
 * 技术点：
 * 1. 定义了好友关系缓存管理的核心功能
 * 2. 作为应用层与基础设施层之间的抽象，实现依赖倒置
 * 3. 支持事件驱动的缓存更新机制
 * 4. 配合领域事件实现数据库与缓存的最终一致性
 */
public interface FriendCacheService {

    /**
     * 更新好友缓存
     * 根据好友事件更新Redis中的好友关系缓存
     * 
     * @param friendEvent 好友事件对象，包含事件类型和相关用户ID
     */
    void updateFriendCache(IMFriendEvent friendEvent);
}
