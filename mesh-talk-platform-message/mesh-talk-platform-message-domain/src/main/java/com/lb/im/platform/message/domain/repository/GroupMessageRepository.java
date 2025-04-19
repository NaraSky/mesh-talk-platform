package com.lb.im.platform.message.domain.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lb.im.platform.common.model.entity.GroupMessage;
import com.lb.im.platform.common.model.vo.GroupMessageVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * 群聊消息数据访问层接口
 * 定义了群聊消息相关的数据库操作方法
 * 继承MyBatis-Plus的BaseMapper接口，提供基础的CRUD方法
 */
public interface GroupMessageRepository extends BaseMapper<GroupMessage> {

    /**
     * 检查指定ID的消息是否存在
     * 使用SQL查询优化，仅返回常数1而不是整个消息对象，提高性能
     * 用于分布式事务的检查阶段，确认消息是否已持久化
     *
     * @param messageId 要检查的消息ID
     * @return 如果消息存在返回1，否则返回null
     */
    @Select("select 1 from im_group_message where id = #{messageId} limit 1")
    Integer checkExists(@Param("messageId") Long messageId);

    @Select({"<script> " +
            "select id as id, group_id as groupId, send_id as sendId, send_nick_name as sendNickName, " +
            "at_user_ids as atUserIdsStr, content as content, type as type, status as status, send_time as sendTime " +
            "from im_group_message where group_id = #{groupId} and send_time <![CDATA[ > ]]> #{sendTime} " +
            "and send_id  <![CDATA[ <> ]]> #{sendId} and status  <![CDATA[ <> ]]> #{status} " +
            " <if test=\"maxReadId != null and maxReadId != 0\"> " +
            " and id <![CDATA[ > ]]> #{maxReadId} " +
            "</if> " +
            "limit #{limitCount}" +
            "</script>"})
    List<GroupMessageVO> getUnreadGroupMessageList(@Param("groupId") Long groupId, @Param("sendTime") Date sendTime,
                                                   @Param("sendId") Long sendId, @Param("status") Integer status,
                                                   @Param("maxReadId") Long maxReadId, @Param("limitCount") Integer limitCount);

    @Select({"<script> " +
            "select id as id, group_id as groupId, send_id as sendId, send_nick_name as sendNickName, " +
            "at_user_ids as atUserIdsStr, content as content, type as type, status as status, send_time as sendTime " +
            "from im_group_message where id <![CDATA[ > ]]> #{minId} and send_time <![CDATA[ > ]]> #{minDate}  and group_id in " +
            "<foreach collection='ids' item='id' index='index' separator=',' open='(' close=')'> " +
            " #{id} " +
            " </foreach> " +
            " and status  <![CDATA[ <> ]]> #{status} " +
            " order by id asc limit #{limitCount} " +
            "</script>"})
    List<GroupMessageVO> loadGroupMessageList(@Param("minId") Long minId, @Param("minDate") Date minDate, @Param("ids") List<Long> ids,
                                              @Param("status") Integer status, @Param("limitCount") Integer limitCount);

    @Select({"<script> " +
            "select id as id, group_id as groupId, send_id as sendId, send_nick_name as sendNickName, " +
            "at_user_ids as atUserIdsStr, content as content, type as type, status as status, send_time as sendTime " +
            "from im_group_message where group_id = #{groupId} and send_time <![CDATA[ > ]]> #{sendTime}   " +
            " and status  <![CDATA[ <> ]]> #{status} order by id desc limit #{stIdx}, #{size}" +
            "</script>"})
    List<GroupMessageVO> getHistoryMessage(@Param("groupId") Long groupId, @Param("sendTime") Date sendTime,
                                           @Param("status") Integer status, @Param("stIdx") long stIdx, @Param("size") long size);

    @Select("select id from im_group_message where group_id = #{groupId} order by id desc limit 1")
    Long getMaxMessageId(@Param("groupId") Long groupId);
}

