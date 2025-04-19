package com.lb.im.platform.message.domain.event;

import com.lb.im.platform.common.model.constants.IMPlatformConstants;
import com.lb.im.platform.common.model.dto.GroupMessageDTO;

import java.util.Date;
import java.util.List;

/**
 * 群组消息事务事件类
 * 用于在分布式事务处理中承载群聊消息相关信息
 * 集成了基础消息事务事件类，并添加了群聊特有的字段
 */
public class IMGroupMessageTxEvent extends IMMessageTxEvent {
    // 消息发送人昵称（在群聊中显示的名称）
    private String sendNickName;
    // 接收消息的用户ID列表（群成员列表，不包括发送者自己）
    private List<Long> userIds;
    // 群聊消息数据，包含消息内容、类型、群ID等信息
    private GroupMessageDTO groupMessageDTO;

    public IMGroupMessageTxEvent() {
    }

    public IMGroupMessageTxEvent(Long id, Long senderId, String sendNickName, Integer terminal, Date sendTime, String destination, List<Long> userIds, GroupMessageDTO groupMessageDTO) {
        // 调用父类构造器，传入基础参数并指定消息类型为群聊消息
        super(id, senderId, terminal, sendTime, destination, IMPlatformConstants.TYPE_MESSAGE_GROUP);
        this.sendNickName = sendNickName;
        this.groupMessageDTO = groupMessageDTO;
        this.userIds = userIds;
    }

    public String getSendNickName() {
        return sendNickName;
    }

    public void setSendNickName(String sendNickName) {
        this.sendNickName = sendNickName;
    }

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }

    public GroupMessageDTO getGroupMessageDTO() {
        return groupMessageDTO;
    }

    public void setGroupMessageDTO(GroupMessageDTO groupMessageDTO) {
        this.groupMessageDTO = groupMessageDTO;
    }
}
