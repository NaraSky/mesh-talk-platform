package com.lb.im.platform.message.domain.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lb.im.platform.common.exception.IMException;
import com.lb.im.platform.common.model.entity.PrivateMessage;
import com.lb.im.platform.common.model.enums.HttpCode;
import com.lb.im.platform.common.model.enums.MessageStatus;
import com.lb.im.platform.common.model.vo.PrivateMessageVO;
import com.lb.im.platform.common.utils.BeanUtils;
import com.lb.im.platform.message.domain.event.IMPrivateMessageTxEvent;
import com.lb.im.platform.message.domain.repository.PrivateMessageRepository;
import com.lb.im.platform.message.domain.service.PrivateMessageDomainService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class PrivateMessageDomainServiceImpl extends ServiceImpl<PrivateMessageRepository, PrivateMessage> implements PrivateMessageDomainService {

    @Override
    public boolean saveIMPrivateMessageSaveEvent(IMPrivateMessageTxEvent privateMessageTxEvent) {
        if (privateMessageTxEvent == null || privateMessageTxEvent.getPrivateMessageDTO() == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        // 保存消息
        PrivateMessage privateMessage = BeanUtils.copyProperties(privateMessageTxEvent.getPrivateMessageDTO(), PrivateMessage.class);
        if (privateMessage == null) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "转换单聊消息失败");
        }
        //设置消息id
        privateMessage.setId(privateMessageTxEvent.getId());
        //设置消息发送人id
        privateMessage.setSendId(privateMessageTxEvent.getSenderId());
        //设置消息状态
        privateMessage.setStatus(MessageStatus.UNSEND.code());
        //设置发送时间
        privateMessage.setSendTime(privateMessageTxEvent.getSendTime());
        //保存数据
        return this.saveOrUpdate(privateMessage);
    }

    @Override
    public boolean checkExists(Long messageId) {
        return baseMapper.checkExists(messageId) != null;
    }

    @Override
    public List<PrivateMessage> getAllUnreadPrivateMessage(Long userId, List<Long> friendIdList) {
        if (userId == null || CollectionUtil.isEmpty(friendIdList)) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        // 获取当前用户所有未读消息
        LambdaQueryWrapper<PrivateMessage> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(PrivateMessage::getRecvId, userId)
                .eq(PrivateMessage::getStatus, MessageStatus.UNSEND)
                .in(PrivateMessage::getSendId, friendIdList);
        return this.list(queryWrapper);
    }

    @Override
    public List<PrivateMessageVO> getPrivateMessageVOList(Long userId, List<Long> friendIds) {
        return baseMapper.getPrivateMessageVOList(userId, friendIds);
    }

    @Override
    public List<PrivateMessageVO> loadMessage(Long userId, Long minId, Date minDate, List<Long> friendIds, int limitCount) {
        return baseMapper.loadMessage(userId, minId, minDate, friendIds, limitCount);
    }

    @Override
    public int batchUpdatePrivateMessageStatus(Integer status, List<Long> ids) {
        return baseMapper.batchUpdatePrivateMessageStatus(status, ids);
    }

    @Override
    public List<PrivateMessageVO> loadMessageByUserIdAndFriendId(Long userId, Long friendId, long stIdx, long size) {
        return baseMapper.loadMessageByUserIdAndFriendId(userId, friendId, stIdx, size);
    }
}
