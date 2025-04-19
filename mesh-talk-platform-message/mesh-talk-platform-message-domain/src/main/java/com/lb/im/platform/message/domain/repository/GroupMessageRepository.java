package com.lb.im.platform.message.domain.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lb.im.platform.common.model.entity.GroupMessage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
}
