package com.lb.im.platform.message.application.service;

import com.lb.im.platform.common.model.dto.PrivateMessageDTO;
import com.lb.im.platform.message.domain.event.IMPrivateMessageTxEvent;

public interface PrivateMessageService {

    /**
     * 发送私聊消息
     *
     * @param dto 私聊消息
     * @return 消息id
     */
    Long sendMessage(PrivateMessageDTO dto);

    /**
     * 保存单聊消息
     */
    boolean saveIMPrivateMessageSaveEvent(IMPrivateMessageTxEvent privateMessageSaveEvent);

    /**
     * 检测数据
     */
    boolean checkExists(Long messageId);
}
