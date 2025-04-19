package com.lb.im.platform.message.application.service;

import com.lb.im.platform.common.model.dto.GroupMessageDTO;
import com.lb.im.platform.common.model.vo.GroupMessageVO;
import com.lb.im.platform.message.domain.event.IMGroupMessageTxEvent;

import java.util.List;

/**
 * 群聊消息应用服务接口
 * 提供群聊消息相关的高级业务功能，包括消息发送和消息事务处理
 * 作为应用层服务，协调领域层服务和外部服务完成复杂业务逻辑
 */
public interface GroupMessageService {

    /**
     * 发送群聊消息
     * 处理群聊消息的发送流程，包括权限校验、消息构建和消息事务处理
     *
     * @param dto 群聊消息数据传输对象，包含消息内容、群组ID等信息
     * @return 生成的消息ID
     */
    Long sendMessage(GroupMessageDTO dto);

    /**
     * 保存群聊消息
     * 处理消息事务事件，将消息持久化到数据库
     * 用于本地事务中实现消息的可靠存储
     *
     * @param imGroupMessageTxEvent 群聊消息事务事件
     * @return 保存是否成功
     */
    boolean saveIMGroupMessageTxEvent(IMGroupMessageTxEvent imGroupMessageTxEvent);

    /**
     * 检测某条消息是否存在
     * 用于分布式事务的检查阶段，确认消息是否已成功持久化
     *
     * @param messageId 消息ID
     * @return 如果消息存在返回true，否则返回false
     */
    boolean checkExists(Long messageId);

    /**
     * 异步拉取群聊消息，通过websocket异步推送
     */
    void pullUnreadMessage();

    /**
     * 拉取消息，只能拉取最近1个月的消息，一次拉取100条
     */
    List<GroupMessageVO> loadMessage(Long minId);

    /**
     * 拉取历史聊天记录
     */
    List<GroupMessageVO> findHistoryMessage(Long groupId, Long page, Long size);
}
