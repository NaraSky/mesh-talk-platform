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

/**
 * 私聊消息领域服务实现类
 * 实现私聊消息相关的核心业务逻辑，处理消息的存储、查询和状态管理等操作
 * 使用MyBatis-Plus的ServiceImpl作为基类，提供基础的CRUD功能
 */
@Service
public class PrivateMessageDomainServiceImpl extends ServiceImpl<PrivateMessageRepository, PrivateMessage> implements PrivateMessageDomainService {

    /**
     * 保存私聊消息事务事件
     * 将事务事件中的数据转换为实体对象并持久化到数据库
     * 
     * 实现步骤：
     * 1. 验证事务事件参数的有效性
     * 2. 将DTO对象转换为实体对象
     * 3. 设置消息的基本属性（ID、发送者、状态、时间）
     * 4. 保存或更新消息实体到数据库
     *
     * @param privateMessageTxEvent 私聊消息事务事件，包含消息的完整信息
     * @return 保存操作是否成功
     * @throws IMException 当参数为空或转换失败时抛出异常
     */
    @Override
    public boolean saveIMPrivateMessageSaveEvent(IMPrivateMessageTxEvent privateMessageTxEvent) {
        // 参数校验
        if (privateMessageTxEvent == null || privateMessageTxEvent.getPrivateMessageDTO() == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        // 保存消息 - 将DTO转换为实体对象
        PrivateMessage privateMessage = BeanUtils.copyProperties(privateMessageTxEvent.getPrivateMessageDTO(), PrivateMessage.class);
        if (privateMessage == null) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "转换单聊消息失败");
        }
        // 设置消息id - 使用事务事件中的ID
        privateMessage.setId(privateMessageTxEvent.getId());
        // 设置消息发送人id - 使用事务事件中的发送者ID
        privateMessage.setSendId(privateMessageTxEvent.getSenderId());
        // 设置消息状态 - 初始状态为未发送
        privateMessage.setStatus(MessageStatus.UNSEND.code());
        // 设置发送时间 - 使用事务事件中的发送时间
        privateMessage.setSendTime(privateMessageTxEvent.getSendTime());
        // 保存数据 - 使用MyBatis-Plus的saveOrUpdate方法
        return this.saveOrUpdate(privateMessage);
    }

    /**
     * 检查指定ID的消息是否存在
     * 用于分布式事务的检查阶段，确认消息是否已成功持久化
     * 
     * 实现方式：
     * 调用Repository层的checkExists方法，该方法返回非空值表示消息存在
     *
     * @param messageId 要检查的消息ID
     * @return 如果消息存在返回true，否则返回false
     */
    @Override
    public boolean checkExists(Long messageId) {
        return baseMapper.checkExists(messageId) != null;
    }

    /**
     * 获取用户所有未读的私聊消息
     * 查询指定用户接收的所有未读消息，并按好友ID列表过滤
     * 
     * 实现步骤：
     * 1. 验证参数的有效性
     * 2. 构建查询条件，筛选未读消息
     * 3. 执行查询并返回结果
     *
     * @param userId       接收消息的用户ID
     * @param friendIdList 好友ID列表，用于过滤消息发送者
     * @return 未读私聊消息实体列表
     * @throws IMException 当参数无效时抛出异常
     */
    @Override
    public List<PrivateMessage> getAllUnreadPrivateMessage(Long userId, List<Long> friendIdList) {
        // 参数校验
        if (userId == null || CollectionUtil.isEmpty(friendIdList)) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        // 获取当前用户所有未读消息 - 使用Lambda查询构建器
        LambdaQueryWrapper<PrivateMessage> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(PrivateMessage::getRecvId, userId)  // 接收者是当前用户
                .eq(PrivateMessage::getStatus, MessageStatus.UNSEND)  // 消息状态为未发送（未读）
                .in(PrivateMessage::getSendId, friendIdList);  // 发送者在好友列表中
        return this.list(queryWrapper);  // 执行查询并返回结果
    }

    /**
     * 获取用户所有未读的私聊消息VO对象
     * 查询指定用户接收的所有未读消息，并转换为前端展示所需的VO对象
     * 
     * 实现方式：
     * 直接调用Repository层的getPrivateMessageVOList方法，该方法返回已转换好的VO对象列表
     *
     * @param userId    接收消息的用户ID
     * @param friendIds 好友ID列表，用于过滤消息发送者
     * @return 未读私聊消息VO对象列表
     */
    @Override
    public List<PrivateMessageVO> getPrivateMessageVOList(Long userId, List<Long> friendIds) {
        return baseMapper.getPrivateMessageVOList(userId, friendIds);
    }

    /**
     * 加载消息历史记录
     * 支持增量拉取，返回指定ID之后的消息
     * 
     * 实现方式：
     * 直接调用Repository层的loadMessage方法，该方法执行复杂的SQL查询
     *
     * @param userId     当前用户ID
     * @param minId      最小消息ID，用于增量拉取
     * @param minDate    最早消息日期，限制查询时间范围
     * @param friendIds  好友ID列表，用于过滤消息发送者和接收者
     * @param limitCount 限制返回消息数量
     * @return 消息历史记录VO对象列表
     */
    @Override
    public List<PrivateMessageVO> loadMessage(Long userId, Long minId, Date minDate, List<Long> friendIds, int limitCount) {
        return baseMapper.loadMessage(userId, minId, minDate, friendIds, limitCount);
    }

    /**
     * 批量更新私聊消息状态
     * 用于标记多条消息为已读、已撤回等状态
     * 
     * 实现方式：
     * 直接调用Repository层的batchUpdatePrivateMessageStatus方法执行批量更新操作
     *
     * @param status 目标消息状态，如已读(1)、已撤回(2)等
     * @param ids    待更新的消息ID列表
     * @return 成功更新的记录数
     */
    @Override
    public int batchUpdatePrivateMessageStatus(Integer status, List<Long> ids) {
        return baseMapper.batchUpdatePrivateMessageStatus(status, ids);
    }

    /**
     * 加载指定用户与好友之间的历史消息
     * 支持分页查询，按消息ID倒序排列
     * 
     * 实现方式：
     * 直接调用Repository层的loadMessageByUserIdAndFriendId方法执行查询
     *
     * @param userId   当前用户ID
     * @param friendId 好友ID
     * @param stIdx    分页起始索引
     * @param size     每页消息数量
     * @return 历史消息VO对象列表，按消息ID倒序排序
     */
    @Override
    public List<PrivateMessageVO> loadMessageByUserIdAndFriendId(Long userId, Long friendId, long stIdx, long size) {
        return baseMapper.loadMessageByUserIdAndFriendId(userId, friendId, stIdx, size);
    }

    /**
     * 将消息更新为已读状态
     * 批量更新指定发送者和接收者之间的所有已发送消息状态
     * 
     * 实现方式：
     * 直接调用Repository层的updateMessageStatus方法执行更新操作
     *
     * @param status 目标消息状态，通常为已读状态(1)
     * @param sendId 消息发送者ID
     * @param recvId 消息接收者ID
     * @return 成功更新的记录数
     */
    @Override
    public int updateMessageStatus(Integer status, Long sendId, Long recvId) {
        return baseMapper.updateMessageStatus(status, sendId, recvId);
    }

    /**
     * 根据消息ID更新消息状态
     * 用于更新单条消息的状态，如标记为已读、已撤回等
     * 
     * 实现方式：
     * 直接调用Repository层的updateMessageStatusById方法执行更新操作
     *
     * @param status    目标消息状态，如已读(1)、已撤回(2)等
     * @param messageId 待更新的消息ID
     * @return 成功更新的记录数，成功为1，失败为0
     */
    @Override
    public int updateMessageStatusById(Integer status, Long messageId) {
        return baseMapper.updateMessageStatusById(status, messageId);
    }

    /**
     * 根据消息ID获取私聊消息详情
     * 查询单条私聊消息的完整信息
     * 
     * 实现方式：
     * 直接调用Repository层的getPrivateMessageById方法执行查询
     *
     * @param messageId 消息ID
     * @return 私聊消息VO对象，包含消息的完整信息
     */
    @Override
    public PrivateMessageVO getPrivateMessageById(Long messageId) {
        return baseMapper.getPrivateMessageById(messageId);
    }
}
