package com.lb.im.platform.message.domain.event;

import com.lb.im.common.domain.event.IMBaseEvent;

import java.util.Date;

/**
 * 消息事务事件基类
 * 用于在分布式事务中承载消息相关的基础信息
 * 继承自IMBaseEvent基础事件类，添加了消息特有的字段
 */
public class IMMessageTxEvent extends IMBaseEvent {

    // 消息发送人ID
    private Long senderId;
    // 终端类型（如PC端、移动端等）
    private Integer terminal;
    // 消息发送时间
    private Date sendTime;
    // 消息类型：type_private(单聊消息)、type_group(群聊消息)
    private String messageType;

    public IMMessageTxEvent() {
    }

    public IMMessageTxEvent(Long id, Long senderId, Integer terminal, Date sendTime, String destination, String messageType) {
        super(id, destination);
        this.senderId = senderId;
        this.terminal = terminal;
        this.sendTime = sendTime;
        this.messageType = messageType;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Integer getTerminal() {
        return terminal;
    }

    public void setTerminal(Integer terminal) {
        this.terminal = terminal;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
