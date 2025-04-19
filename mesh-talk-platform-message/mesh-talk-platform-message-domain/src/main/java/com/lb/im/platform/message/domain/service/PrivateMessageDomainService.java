package com.lb.im.platform.message.domain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lb.im.platform.common.model.entity.PrivateMessage;
import com.lb.im.platform.message.domain.event.IMPrivateMessageTxEvent;

public interface PrivateMessageDomainService extends IService<PrivateMessage> {
    /**
     * 保存单聊消息
     */
    boolean saveIMPrivateMessageSaveEvent(IMPrivateMessageTxEvent privateMessageSaveEvent);

    /**
     * 检测某条消息是否存在
     */
    boolean checkExists(Long messageId);
}
