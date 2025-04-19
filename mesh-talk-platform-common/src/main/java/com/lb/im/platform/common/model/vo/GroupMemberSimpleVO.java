package com.lb.im.platform.common.model.vo;

import com.alibaba.fastjson.JSON;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

@ApiModel("群成员简易信息VO")
public class GroupMemberSimpleVO implements Serializable {

    private static final long serialVersionUID = -1878612751093903962L;
    @ApiModelProperty("群内显示名称")
    private String aliasName;

    @ApiModelProperty("是否已退出")
    private Boolean quit;

    @ApiModelProperty("群组id")
    private Long groupId;

    @ApiModelProperty("创建时间")
    private Date createdTime;

    public GroupMemberSimpleVO() {
    }

    public GroupMemberSimpleVO(String aliasName, Boolean quit) {
        this.aliasName = aliasName;
        this.quit = quit;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public Boolean getQuit() {
        return quit;
    }

    public void setQuit(Boolean quit) {
        this.quit = quit;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
