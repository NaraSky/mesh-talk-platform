package com.lb.im.platform.friend.domain.event;

import com.lb.im.common.domain.event.IMBaseEvent;

/**
 * 好友关系领域事件类
 * 
 * 技术点：
 * 1. 基于领域驱动设计(DDD)中的领域事件模式
 * 2. 继承自IMBaseEvent基类，实现事件的基本属性和行为
 * 3. 用于在好友关系变更时发布事件，实现系统解耦和异步通知
 * 4. 支持多种消息队列实现（RocketMQ和COLA事件总线）
 */
public class IMFriendEvent extends IMBaseEvent {
    /**
     * 操作类型
     * 表示好友关系的操作类型，如绑定(bind)、解绑(unbind)、更新(update)
     */
    private String handler;

    /**
     * 好友ID
     * 表示相关好友的用户ID
     */
    private Long friendId;

    /**
     * 默认构造函数
     * 用于序列化/反序列化
     */
    public IMFriendEvent() {
    }

    public IMFriendEvent(Long id, Long friendId, String handler, String destination) {
        super(id, destination);
        this.handler = handler;
        this.friendId = friendId;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public Long getFriendId() {
        return friendId;
    }

    public void setFriendId(Long friendId) {
        this.friendId = friendId;
    }
}
