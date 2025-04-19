package com.lb.im.platform.message.application.service;

import com.lb.im.platform.common.model.dto.PrivateMessageDTO;
import com.lb.im.platform.common.model.vo.PrivateMessageVO;
import com.lb.im.platform.message.domain.event.IMPrivateMessageTxEvent;

import java.util.List;

/**
 * 私聊消息应用服务接口
 * 提供私聊消息相关的高级业务功能，包括消息发送、消息事务处理和消息状态管理
 * 作为应用层服务，协调领域层服务和外部服务完成复杂业务逻辑
 */
public interface PrivateMessageService {

    /**
     * 发送私聊消息
     * 处理私聊消息的发送流程，包括权限校验、消息构建和消息事务处理
     * 
     * @param dto 私聊消息数据传输对象，包含消息内容、接收者ID等信息
     * @return 生成的消息ID
     */
    Long sendMessage(PrivateMessageDTO dto);

    /**
     * 保存私聊消息事务事件
     * 处理消息事务事件，将消息持久化到数据库
     * 用于本地事务中实现消息的可靠存储
     * 
     * @param privateMessageSaveEvent 私聊消息事务事件
     * @return 保存是否成功
     */
    boolean saveIMPrivateMessageSaveEvent(IMPrivateMessageTxEvent privateMessageSaveEvent);

    /**
     * 检查消息是否存在
     * 用于分布式事务的检查阶段，确认消息是否已成功持久化
     * 
     * @param messageId 消息ID
     * @return 如果消息存在返回true，否则返回false
     */
    boolean checkExists(Long messageId);

    /**
     * 异步拉取未读私聊消息
     * 获取当前用户的所有未读私聊消息，并通过WebSocket异步推送给用户
     * 用于用户上线时或定时拉取未读消息
     */
    void pullUnreadMessage();

    /**
     * 加载消息历史记录
     * 增量拉取指定ID之后的私聊消息，限制为最近1个月内的消息
     * 用于客户端初始化或滚动加载更多消息
     * 
     * @param minId 最小消息ID，用于增量拉取，返回ID大于此值的消息
     * @return 消息历史记录VO对象列表，按消息ID升序排序，最多返回100条
     */
    List<PrivateMessageVO> loadMessage(Long minId);

    /**
     * 获取与指定好友的历史聊天记录
     * 分页获取与特定好友的历史私聊消息
     * 
     * @param friendId 好友用户ID
     * @param page 页码，从1开始
     * @param size 每页大小
     * @return 历史消息VO对象列表，按消息ID倒序排序
     */
    List<PrivateMessageVO> getHistoryMessage(Long friendId, Long page, Long size);

    /**
     * 标记消息为已读状态
     * 将与指定好友的整个会话中的所有消息都置为已读状态
     * 同时会通知其他终端同步已读状态
     * 
     * @param friendId 好友用户ID，消息发送者
     */
    void readedMessage(Long friendId);

    /**
     * 撤回消息
     * 将指定ID的消息标记为已撤回状态
     * 撤回的消息在客户端会显示为"消息已撤回"
     * 
     * @param id 要撤回的消息ID
     */
    void recallMessage(Long id);
}
