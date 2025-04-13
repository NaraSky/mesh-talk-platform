package com.lb.im.platform.common.session;

import com.lb.im.common.domain.model.IMSessionInfo;

public class UserSession extends IMSessionInfo {

    /*
     * 用户名称
     */
    private String userName;

    /*
     * 用户昵称
     */
    private String nickName;

    public UserSession() {
    }

    public UserSession(Long userId, Integer terminal, String userName, String nickName) {
        super(userId, terminal);
        this.userName = userName;
        this.nickName = nickName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}
