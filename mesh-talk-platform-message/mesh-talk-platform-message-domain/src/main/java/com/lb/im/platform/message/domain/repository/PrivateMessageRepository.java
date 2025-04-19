package com.lb.im.platform.message.domain.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lb.im.platform.common.model.entity.PrivateMessage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface PrivateMessageRepository extends BaseMapper<PrivateMessage> {

    @Select("select 1 from im_private_message where id = #{messageId} limit 1")
    Integer checkExists(@Param("messageId") Long messageId);
}
