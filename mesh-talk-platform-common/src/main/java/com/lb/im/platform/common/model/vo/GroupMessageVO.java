package com.lb.im.platform.common.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.lb.im.platform.common.serializer.DateToLongSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
import java.util.List;

@ApiModel("群消息VO")
public class GroupMessageVO {

    @ApiModelProperty(value = "消息id")
    private Long id;

    @ApiModelProperty(value = "群聊id")
    private Long groupId;

    @ApiModelProperty(value = " 发送者id")
    private Long sendId;

    @ApiModelProperty(value = " 发送者昵称")
    private String sendNickName;

    @ApiModelProperty(value = "消息内容")
    private String content;

    @ApiModelProperty(value = "消息内容类型 具体枚举值由应用层定义")
    private Integer type;

    @ApiModelProperty(value = "@用户列表")
    private List<Long> atUserIds;

    @ApiModelProperty(value = "@用户列表")
    private String atUserIdsStr;

    @ApiModelProperty(value = " 状态")
    private Integer status;

    @ApiModelProperty(value = "发送时间")
    @JsonSerialize(using = DateToLongSerializer.class)
    private Date sendTime;

    public GroupMessageVO() {
    }

    public GroupMessageVO(Long id, Long groupId, Long sendId, String sendNickName, String content, Integer type, List<Long> atUserIds, Integer status, Date sendTime) {
        this.id = id;
        this.groupId = groupId;
        this.sendId = sendId;
        this.sendNickName = sendNickName;
        this.content = content;
        this.type = type;
        this.atUserIds = atUserIds;
        this.status = status;
        this.sendTime = sendTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getSendId() {
        return sendId;
    }

    public void setSendId(Long sendId) {
        this.sendId = sendId;
    }

    public String getSendNickName() {
        return sendNickName;
    }

    public void setSendNickName(String sendNickName) {
        this.sendNickName = sendNickName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public List<Long> getAtUserIds() {
        return atUserIds;
    }

    public void setAtUserIds(List<Long> atUserIds) {
        this.atUserIds = atUserIds;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public String getAtUserIdsStr() {
        return atUserIdsStr;
    }

    public void setAtUserIdsStr(String atUserIdsStr) {
        this.atUserIdsStr = atUserIdsStr;
    }
}
