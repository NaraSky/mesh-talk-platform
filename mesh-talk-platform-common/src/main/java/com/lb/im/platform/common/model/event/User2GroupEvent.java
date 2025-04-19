package com.lb.im.platform.common.model.event;

import com.lb.im.common.domain.event.IMBaseEvent;

public class User2GroupEvent extends IMBaseEvent {

    //用户头像缩略图
    private String headImageThumb;

    public User2GroupEvent() {
    }

    public User2GroupEvent(Long id, String headImageThumb, String destination) {
        super(id, destination);
        this.headImageThumb = headImageThumb;
    }

    public String getHeadImageThumb() {
        return headImageThumb;
    }

    public void setHeadImageThumb(String headImageThumb) {
        this.headImageThumb = headImageThumb;
    }
}
