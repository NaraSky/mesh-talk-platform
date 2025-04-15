package com.lb.im.platform.user.domain.event;

import com.lb.im.common.domain.event.IMBaseEvent;

public class IMUserEvent extends IMBaseEvent {

    private String userName;

    public IMUserEvent(Long id, String userName, String destination) {
        super(id, destination);
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

}
