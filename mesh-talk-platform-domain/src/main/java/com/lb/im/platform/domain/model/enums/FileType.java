package com.lb.im.platform.domain.model.enums;

public enum FileType {

    FILE(0,"文件"),
    IMAGE(1,"图片"),
    VIDEO(2,"视频"),
    AUDIO(3,"语音");

    private final Integer code;

    private final String desc;

    FileType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer code(){
        return this.code;
    }
}
