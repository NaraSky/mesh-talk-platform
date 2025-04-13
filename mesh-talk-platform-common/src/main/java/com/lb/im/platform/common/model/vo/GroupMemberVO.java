package com.lb.im.platform.common.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("群成员信息VO")
public class GroupMemberVO {

    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("群内显示名称")
    private String aliasName;

    @ApiModelProperty("头像")
    private String headImage;

    @ApiModelProperty("是否已退出")
    private Boolean quit;

    @ApiModelProperty(value = "是否在线")
    private Boolean online;

    @ApiModelProperty("备注")
    private String remark;

    public GroupMemberVO() {
    }

    public GroupMemberVO(Long userId, String aliasName, String headImage, Boolean quit, Boolean online, String remark) {
        this.userId = userId;
        this.aliasName = aliasName;
        this.headImage = headImage;
        this.quit = quit;
        this.online = online;
        this.remark = remark;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public String getHeadImage() {
        return headImage;
    }

    public void setHeadImage(String headImage) {
        this.headImage = headImage;
    }

    public Boolean getQuit() {
        return quit;
    }

    public void setQuit(Boolean quit) {
        this.quit = quit;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
