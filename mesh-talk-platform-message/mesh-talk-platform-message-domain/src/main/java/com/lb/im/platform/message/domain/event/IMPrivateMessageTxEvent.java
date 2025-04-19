package com.lb.im.platform.message.domain.event;

import com.lb.im.platform.common.model.constants.IMPlatformConstants;
import com.lb.im.platform.common.model.dto.PrivateMessageDTO;

import java.util.Date;

public class IMPrivateMessageTxEvent extends IMMessageTxEvent {
    //消息数据
    private PrivateMessageDTO privateMessageDTO;

    public IMPrivateMessageTxEvent() {
    }

    public IMPrivateMessageTxEvent(Long id, Long senderId, Integer terminal, String destination, Date sendTime, PrivateMessageDTO privateMessageDTO) {
        super(id, senderId, terminal, sendTime, destination, IMPlatformConstants.TYPE_MESSAGE_PRIVATE);
        this.privateMessageDTO = privateMessageDTO;
    }

    public PrivateMessageDTO getPrivateMessageDTO() {
        return privateMessageDTO;
    }

    public void setPrivateMessageDTO(PrivateMessageDTO privateMessageDTO) {
        this.privateMessageDTO = privateMessageDTO;
    }
}
