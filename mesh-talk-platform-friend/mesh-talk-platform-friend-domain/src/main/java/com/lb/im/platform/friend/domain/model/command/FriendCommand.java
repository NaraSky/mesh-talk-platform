package com.lb.im.platform.friend.domain.model.command;

/**
 * 好友命令模型类
 * 用于封装好友操作的参数，如添加好友、删除好友等操作
 * 技术：
 * 1. 基于DDD的命令模式设计
 * 2. 作为领域模型的一部分，用于传递操作参数
 */
public class FriendCommand {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 好友ID
     */
    private Long friendId;

    /**
     * 无参构造函数
     */
    public FriendCommand() {
    }

    /**
     * 带参构造函数
     * 
     * @param userId 用户ID
     * @param friendId 好友ID
     */
    public FriendCommand(Long userId, Long friendId) {
        this.userId = userId;
        this.friendId = friendId;
    }

    /**
     * 检查命令对象是否为空
     * 
     * @return 如果userId或friendId为null则返回true，否则返回false
     */
    public boolean isEmpty(){
        return userId == null || friendId == null;
    }

    /**
     * 获取用户ID
     * 
     * @return 用户ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置用户ID
     * 
     * @param userId 用户ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 获取好友ID
     * 
     * @return 好友ID
     */
    public Long getFriendId() {
        return friendId;
    }

    /**
     * 设置好友ID
     * 
     * @param friendId 好友ID
     */
    public void setFriendId(Long friendId) {
        this.friendId = friendId;
    }
}
