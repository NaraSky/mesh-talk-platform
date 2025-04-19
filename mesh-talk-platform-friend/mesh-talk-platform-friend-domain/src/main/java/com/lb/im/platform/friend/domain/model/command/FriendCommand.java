package com.lb.im.platform.friend.domain.model.command;

/**
 * 好友命令模型类
 * 用于封装好友操作的参数，如添加好友、删除好友等操作
 * 技术：
 * 1. 基于DDD的命令模式设计
 * 2. 作为领域模型的一部分，用于传递操作参数
 */
public class FriendCommand {

    private Long userId;

    private Long friendId;

    public FriendCommand() {
    }

    public FriendCommand(Long userId, Long friendId) {
        this.userId = userId;
        this.friendId = friendId;
    }

    public boolean isEmpty(){
        return userId == null || friendId == null;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getFriendId() {
        return friendId;
    }

    public void setFriendId(Long friendId) {
        this.friendId = friendId;
    }
}
