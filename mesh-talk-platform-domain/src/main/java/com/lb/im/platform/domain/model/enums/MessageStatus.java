package com.lb.im.platform.domain.model.enums;

public enum MessageStatus {

    UNSEND(0,"未发送"),
    SENDED(1,"送达"),
    RECALL(2,"撤回"),
    READED(3,"已读");

    private final Integer code;

    private final String desc;

    MessageStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer code(){
        return this.code;
    }

}
