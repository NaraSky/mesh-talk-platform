package com.lb.im.platform.message.domain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lb.im.platform.common.model.entity.PrivateMessage;
import com.lb.im.platform.common.model.vo.PrivateMessageVO;
import com.lb.im.platform.message.domain.event.IMPrivateMessageTxEvent;

import java.util.Date;
import java.util.List;

public interface PrivateMessageDomainService extends IService<PrivateMessage> {
    /**
     * 保存单聊消息
     */
    boolean saveIMPrivateMessageSaveEvent(IMPrivateMessageTxEvent privateMessageSaveEvent);

    /**
     * 检测某条消息是否存在
     */
    boolean checkExists(Long messageId);

    /**
     * 获取所有未读的消息
     */
    List<PrivateMessage> getAllUnreadPrivateMessage(Long userId, List<Long> friendIdList);

    /**
     * 获取所有未读的消息
     */
    List<PrivateMessageVO> getPrivateMessageVOList(Long userId, List<Long> friendIds);

    /**
     * 拉取消息
     */
    List<PrivateMessageVO> loadMessage(Long userId, Long minId, Date minDate, List<Long> friendIds, int limitCount);

    /**
     * 批量更新状态
     */
    int batchUpdatePrivateMessageStatus(Integer status, List<Long> ids);

    /**
     * 拉取指定用户与好友的历史消息
     */
    List<PrivateMessageVO> loadMessageByUserIdAndFriendId(Long userId, Long friendId, long stIdx, long size);
}
