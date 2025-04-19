package com.lb.im.platform.message.domain.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lb.im.platform.common.exception.IMException;
import com.lb.im.platform.common.model.entity.GroupMessage;
import com.lb.im.platform.common.model.enums.HttpCode;
import com.lb.im.platform.common.model.enums.MessageStatus;
import com.lb.im.platform.common.model.vo.GroupMessageVO;
import com.lb.im.platform.common.utils.BeanUtils;
import com.lb.im.platform.message.domain.event.IMGroupMessageTxEvent;
import com.lb.im.platform.message.domain.repository.GroupMessageRepository;
import com.lb.im.platform.message.domain.service.GroupMessageDomainService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 群聊消息领域服务实现类
 * 实现群聊消息相关的核心业务逻辑，处理消息的存储和查询操作
 * 使用MyBatis-Plus的ServiceImpl作为基类，提供基础的CRUD功能
 */
@Service
public class GroupMessageDomainServiceImpl extends ServiceImpl<GroupMessageRepository, GroupMessage> implements GroupMessageDomainService {

    /**
     * 保存群聊消息事务事件
     * 将事务事件中的数据转换为实体对象并持久化
     *
     * @param imGroupMessageTxEvent 群聊消息事务事件
     * @return 保存操作是否成功
     * @throws IMException 参数为空或转换失败时抛出异常
     */
    @Override
    public boolean saveIMGroupMessageTxEvent(IMGroupMessageTxEvent imGroupMessageTxEvent) {
        // 参数校验
        if (imGroupMessageTxEvent == null || imGroupMessageTxEvent.getGroupMessageDTO() == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }

        // 事件对象转换为消息实体
        GroupMessage groupMessage = BeanUtils.copyProperties(imGroupMessageTxEvent, GroupMessage.class);
        if (groupMessage == null) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "转换群聊消息失败");
        }

        // 设置消息实体属性
        groupMessage.setId(imGroupMessageTxEvent.getId());  // 使用事件中的ID作为消息ID
        groupMessage.setGroupId(imGroupMessageTxEvent.getGroupMessageDTO().getGroupId());  // 设置群组ID
        groupMessage.setSendId(imGroupMessageTxEvent.getSenderId());  // 设置发送者ID
        groupMessage.setSendNickName(imGroupMessageTxEvent.getSendNickName());  // 设置发送者昵称
        groupMessage.setSendTime(imGroupMessageTxEvent.getSendTime());  // 设置发送时间
        groupMessage.setContent(imGroupMessageTxEvent.getGroupMessageDTO().getContent());  // 设置消息内容
        groupMessage.setType(imGroupMessageTxEvent.getGroupMessageDTO().getType());  // 设置消息类型
        groupMessage.setStatus(MessageStatus.UNSEND.code());  // 设置消息状态为未发送

        // 处理@用户功能，将@的用户ID列表转换为逗号分隔的字符串
        if (CollectionUtil.isNotEmpty(imGroupMessageTxEvent.getGroupMessageDTO().getAtUserIds())) {
            groupMessage.setAtUserIds(StrUtil.join(",", imGroupMessageTxEvent.getGroupMessageDTO().getAtUserIds()));
        }

        // 保存或更新消息实体
        return this.saveOrUpdate(groupMessage);
    }

    /**
     * 检查消息是否存在
     * 用于分布式事务的检查阶段，确认消息是否已持久化
     *
     * @param messageId 消息ID
     * @return 如果消息存在返回true，否则返回false
     */
    @Override
    public boolean checkExists(Long messageId) {
        return baseMapper.checkExists(messageId) != null;
    }

    @Override
    public List<GroupMessageVO> getUnreadGroupMessageList(Long groupId, Date sendTime, Long sendId, Integer status, Long maxReadId, Integer limitCount) {
        return baseMapper.getUnreadGroupMessageList(groupId, sendTime, sendId, status, maxReadId, limitCount);
    }

    @Override
    public List<GroupMessageVO> loadGroupMessageList(Long minId, Date minDate, List<Long> ids, Integer status, Integer limitCount) {
        return baseMapper.loadGroupMessageList(minId, minDate, ids, status, limitCount);
    }

    @Override
    public List<GroupMessageVO> getHistoryMessage(Long groupId, Date sendTime, Integer status, long stIdx, long size) {
        return baseMapper.getHistoryMessage(groupId, sendTime, status, stIdx, size);
    }
}
