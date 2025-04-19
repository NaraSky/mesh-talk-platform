package com.lb.im.platform.message.domain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lb.im.platform.common.model.entity.GroupMessage;
import com.lb.im.platform.message.domain.event.IMGroupMessageTxEvent;

/**
 * 群聊消息领域服务接口
 * 定义了与群聊消息相关的核心业务逻辑，包含对群消息的增删改查等操作
 * 继承MyBatis-Plus的IService接口，提供基础的CRUD功能
 */
public interface GroupMessageDomainService extends IService<GroupMessage> {
    /**
     * 保存群聊消息
     * 将群聊消息事务事件转换为实体并持久化到数据库
     * 
     * @param imGroupMessageTxEvent 群聊消息事务事件，包含消息的完整信息
     * @return 保存是否成功
     */
    boolean saveIMGroupMessageTxEvent(IMGroupMessageTxEvent imGroupMessageTxEvent);

    /**
     * 检测某条消息是否存在
     * 用于分布式事务处理中的事务检查，确认消息是否已成功保存
     * 
     * @param messageId 消息ID
     * @return 如果消息存在返回true，否则返回false
     */
    boolean checkExists(Long messageId);
}

