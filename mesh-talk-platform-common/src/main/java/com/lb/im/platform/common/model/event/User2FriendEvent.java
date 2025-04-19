package com.lb.im.platform.common.model.event;

import com.lb.im.common.domain.event.IMBaseEvent;

public class User2FriendEvent extends IMBaseEvent {
    //昵称
    private String nickName;
    //头像
    private String headImg;

    public User2FriendEvent() {
    }

    public User2FriendEvent(Long id, String nickName, String headImg, String destination) {
        super(id, destination);
        this.nickName = nickName;
        this.headImg = headImg;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getHeadImg() {
        return headImg;
    }

    public void setHeadImg(String headImg) {
        this.headImg = headImg;
    }
}
