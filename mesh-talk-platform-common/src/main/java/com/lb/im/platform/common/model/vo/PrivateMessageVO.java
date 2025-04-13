package com.lb.im.platform.common.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.lb.im.platform.common.serializer.DateToLongSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

@ApiModel("私聊消息VO")
public class PrivateMessageVO {

    @ApiModelProperty(value = " 消息id")
    private Long id;

    @ApiModelProperty(value = " 发送者id")
    private Long sendId;

    @ApiModelProperty(value = " 接收者id")
    private Long recvId;

    @ApiModelProperty(value = " 发送内容")
    private String content;

    @ApiModelProperty(value = "消息内容类型 IMCmdType")
    private Integer type;

    @ApiModelProperty(value = " 状态")
    private Integer status;

    @ApiModelProperty(value = " 发送时间")
    @JsonSerialize(using = DateToLongSerializer.class)
    private Date sendTime;

    public PrivateMessageVO() {
    }

    public PrivateMessageVO(Long id, Long sendId, Long recvId, String content, Integer type, Integer status, Date sendTime) {
        this.id = id;
        this.sendId = sendId;
        this.recvId = recvId;
        this.content = content;
        this.type = type;
        this.status = status;
        this.sendTime = sendTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSendId() {
        return sendId;
    }

    public void setSendId(Long sendId) {
        this.sendId = sendId;
    }

    public Long getRecvId() {
        return recvId;
    }

    public void setRecvId(Long recvId) {
        this.recvId = recvId;
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
}
