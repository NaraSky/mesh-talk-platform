package com.lb.im.platform.common.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

@ApiModel("好友信息VO")
public class FriendVO {

    @NotNull(message = "好友id不可为空")
    @ApiModelProperty(value = "好友id")
    private Long id;

    @NotNull(message = "好友昵称不可为空")
    @ApiModelProperty(value = "好友昵称")
    private String nickName;


    @ApiModelProperty(value = "好友头像")
    private String headImage;

    public FriendVO() {
    }

    public FriendVO(Long id, String nickName, String headImage) {
        this.id = id;
        this.nickName = nickName;
        this.headImage = headImage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getHeadImage() {
        return headImage;
    }

    public void setHeadImage(String headImage) {
        this.headImage = headImage;
    }
}