package com.lb.im.platform.message.domain.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lb.im.platform.common.model.entity.PrivateMessage;
import com.lb.im.platform.common.model.vo.PrivateMessageVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;

/**
 * 私聊消息数据库操作仓库
 * 提供私聊消息的查询、更新等数据库操作
 * 使用MyBatis-Plus和自定义SQL实现复杂查询
 */
public interface PrivateMessageRepository extends BaseMapper<PrivateMessage> {

    /**
     * 检查消息是否存在
     * 用于事务消息的回查机制
     *
     * @param messageId 消息ID
     * @return 如存在返回1，否则为null
     */
    @Select("select 1 from im_private_message where id = #{messageId} limit 1")
    Integer checkExists(@Param("messageId") Long messageId);

    /**
     * 获取指定用户未读的私聊消息列表
     * 按指定好友ID列表过滤消息发送者
     *
     * @param userId    接收消息的用户ID
     * @param friendIds 好友ID列表，用于过滤消息发送者
     * @return 私聊消息VO列表
     */
    @Select({"<script> " +
            "select id as id, send_id as sendId, recv_id as recvId, content as content, type as type, status as status, send_time as sendTime " +
            "from im_private_message where recv_id = #{userId} and status = 0 and send_id in   " +
            "<foreach collection='friendIds' item='friendId' index='index' separator=',' open='(' close=')'> " +
            " #{friendId} " +
            " </foreach> " +
            "</script>"})
    List<PrivateMessageVO> getPrivateMessageVOList(@Param("userId") Long userId, @Param("friendIds") List<Long> friendIds);

    /**
     * 加载消息历史记录
     * 支持增量拉取，返回指定ID之后的消息
     * 包含用户与好友之间的双向消息
     *
     * @param userId     当前用户ID
     * @param minId      最小消息ID，用于增量拉取
     * @param minDate    最早消息日期，限制查询时间范围
     * @param friendIds  好友ID列表
     * @param limitCount 限制返回消息数量
     * @return 消息历史记录列表
     */
    @Select({"<script> " +
            "select id as id, send_id as sendId, recv_id as recvId, content as content, type as type, status as status, send_time as sendTime " +
            "from im_private_message where id  <![CDATA[ > ]]> #{minId} and send_time  <![CDATA[ >= ]]> #{minDate} and status  <![CDATA[ <> ]]> 2 and ( " +
            " ( " +
            "send_id = #{userId} and recv_id in " +
            "<foreach collection='friendIds' item='friendId' index='index' separator=',' open='(' close=')'> " +
            " #{friendId} " +
            " </foreach> " +
            " ) " +
            " or " +
            " (" +
            "recv_id = #{userId} and send_id in " +
            "<foreach collection='friendIds' item='friendId' separator=',' open='(' close=')'> " +
            " #{friendId} " +
            " </foreach> " +
            ") " +
            " ) order by id asc limit #{limitCount} " +
            "</script>"})
    List<PrivateMessageVO> loadMessage(@Param("userId") Long userId, @Param("minId") Long minId, @Param("minDate") Date minDate, @Param("friendIds") List<Long> friendIds, @Param("limitCount") int limitCount);

    /**
     * 批量更新私聊消息状态
     * 用于标记消息为已读、已撤回等状态
     *
     * @param status 目标消息状态
     * @param ids    待更新的消息ID列表
     * @return 成功更新的记录数
     */
    @Update({"<script> " +
            "update im_private_message set status = #{status} where id in " +
            " <foreach collection='ids' item='id' index='index' separator=',' open='(' close=')'>  " +
            " #{id} " +
            " </foreach> " +
            "</script>"})
    int batchUpdatePrivateMessageStatus(@Param("status") Integer status, @Param("ids") List<Long> ids);

    /**
     * 加载指定用户与好友之间的历史消息
     * 支持分页查询，按消息ID倒序排列
     *
     * @param userId   当前用户ID
     * @param friendId 好友ID
     * @param stIdx    分页起始索引
     * @param size     每页消息数量
     * @return 历史消息列表
     */
    @Select({"<script> " +
            "select id as id, send_id as sendId, recv_id as recvId, content as content, type as type, status as status, send_time as sendTime " +
            "from im_private_message where " +
            " ((send_id = #{userId} and recv_id = #{friendId}) or (send_id = #{friendId} and recv_id = #{userId})) " +
            "and status  <![CDATA[ <> ]]> 2 order by id desc limit #{stIdx}, #{size} " +
            "</script>"})
    List<PrivateMessageVO> loadMessageByUserIdAndFriendId(@Param("userId") Long userId, @Param("friendId") Long friendId, @Param("stIdx") long stIdx, @Param("size") long size);
}