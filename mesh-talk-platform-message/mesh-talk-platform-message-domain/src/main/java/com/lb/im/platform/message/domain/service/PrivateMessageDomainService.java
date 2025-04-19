package com.lb.im.platform.message.domain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lb.im.platform.common.model.entity.PrivateMessage;
import com.lb.im.platform.common.model.vo.PrivateMessageVO;
import com.lb.im.platform.message.domain.event.IMPrivateMessageTxEvent;

import java.util.Date;
import java.util.List;

/**
 * 私聊消息领域服务接口
 * 定义了私聊消息相关的核心业务逻辑操作
 * 作为领域层服务，处理私聊消息的存储、查询和状态管理等基础功能
 * 继承MyBatis-Plus的IService接口，提供基础的CRUD功能
 */
public interface PrivateMessageDomainService extends IService<PrivateMessage> {
    /**
     * 保存私聊消息事务事件
     * 将事务事件中的数据转换为实体对象并持久化到数据库
     *
     * @param privateMessageSaveEvent 私聊消息事务事件，包含消息的完整信息
     * @return 保存操作是否成功
     */
    boolean saveIMPrivateMessageSaveEvent(IMPrivateMessageTxEvent privateMessageSaveEvent);

    /**
     * 检查指定ID的消息是否存在
     * 用于分布式事务的检查阶段，确认消息是否已成功持久化
     *
     * @param messageId 要检查的消息ID
     * @return 如果消息存在返回true，否则返回false
     */
    boolean checkExists(Long messageId);

    /**
     * 获取用户所有未读的私聊消息
     * 查询指定用户接收的所有未读消息，并按好友ID列表过滤
     *
     * @param userId       接收消息的用户ID
     * @param friendIdList 好友ID列表，用于过滤消息发送者
     * @return 未读私聊消息实体列表
     */
    List<PrivateMessage> getAllUnreadPrivateMessage(Long userId, List<Long> friendIdList);

    /**
     * 获取用户所有未读的私聊消息VO对象
     * 查询指定用户接收的所有未读消息，并转换为前端展示所需的VO对象
     *
     * @param userId    接收消息的用户ID
     * @param friendIds 好友ID列表，用于过滤消息发送者
     * @return 未读私聊消息VO对象列表
     */
    List<PrivateMessageVO> getPrivateMessageVOList(Long userId, List<Long> friendIds);

    /**
     * 加载消息历史记录
     * 支持增量拉取，返回指定ID之后的消息
     * 包含用户与好友之间的双向消息
     *
     * @param userId     当前用户ID
     * @param minId      最小消息ID，用于增量拉取，返回ID大于此值的消息
     * @param minDate    最早消息日期，限制查询时间范围
     * @param friendIds  好友ID列表，用于过滤消息发送者和接收者
     * @param limitCount 限制返回消息数量
     * @return 消息历史记录VO对象列表，按消息ID升序排序
     */
    List<PrivateMessageVO> loadMessage(Long userId, Long minId, Date minDate, List<Long> friendIds, int limitCount);

    /**
     * 批量更新私聊消息状态
     * 用于标记多条消息为已读、已撤回等状态
     *
     * @param status 目标消息状态，如已读(1)、已撤回(2)等
     * @param ids    待更新的消息ID列表
     * @return 成功更新的记录数
     */
    int batchUpdatePrivateMessageStatus(Integer status, List<Long> ids);

    /**
     * 加载指定用户与好友之间的历史消息
     * 支持分页查询，按消息ID倒序排列
     *
     * @param userId   当前用户ID
     * @param friendId 好友ID
     * @param stIdx    分页起始索引
     * @param size     每页消息数量
     * @return 历史消息VO对象列表，按消息ID倒序排序
     */
    List<PrivateMessageVO> loadMessageByUserIdAndFriendId(Long userId, Long friendId, long stIdx, long size);

    /**
     * 将消息更新为已读状态
     * 批量更新指定发送者和接收者之间的所有已发送消息状态
     *
     * @param status 目标消息状态，通常为已读状态(1)
     * @param sendId 消息发送者ID
     * @param recvId 消息接收者ID
     * @return 成功更新的记录数
     */
    int updateMessageStatus(Integer status, Long sendId, Long recvId);

    /**
     * 根据消息ID更新消息状态
     * 用于更新单条消息的状态，如标记为已读、已撤回等
     *
     * @param status    目标消息状态，如已读(1)、已撤回(2)等
     * @param messageId 待更新的消息ID
     * @return 成功更新的记录数，成功为1，失败为0
     */
    int updateMessageStatusById(Integer status, Long messageId);

    /**
     * 根据消息ID获取私聊消息详情
     * 查询单条私聊消息的完整信息
     *
     * @param messageId 消息ID
     * @return 私聊消息VO对象，包含消息的完整信息
     */
    PrivateMessageVO getPrivateMessageById(Long messageId);
}
