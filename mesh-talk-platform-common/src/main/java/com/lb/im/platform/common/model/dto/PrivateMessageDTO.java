package com.lb.im.platform.common.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@ApiModel("私聊消息DTO")
public class PrivateMessageDTO {

    @NotNull(message = "接收用户id不可为空")
    @ApiModelProperty(value = "接收用户id")
    private Long recvId;


    @Length(max = 1024, message = "内容长度不得大于1024")
    @NotEmpty(message = "发送内容不可为空")
    @ApiModelProperty(value = "发送内容")
    private String content;

    @NotNull(message = "消息类型不可为空")
    @ApiModelProperty(value = "消息类型")
    private Integer type;

    public PrivateMessageDTO() {
    }

    public PrivateMessageDTO(Long recvId, String content, Integer type) {
        this.recvId = recvId;
        this.content = content;
        this.type = type;
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
}