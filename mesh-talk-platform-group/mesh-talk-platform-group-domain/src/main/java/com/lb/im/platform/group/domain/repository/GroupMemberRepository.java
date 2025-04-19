package com.lb.im.platform.group.domain.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lb.im.platform.common.model.entity.GroupMember;
import com.lb.im.platform.common.model.vo.GroupMemberVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface GroupMemberRepository extends BaseMapper<GroupMember> {

    @Select("select user_id as userId, alias_name as aliasName, head_image as headImage, quit as quit, " +
            "remark as remark from im_group_member where group_id = #{groupId} and quit = 0")
    List<GroupMemberVO> getGroupMemberVoListByGroupId(@Param("groupId") Long groupId);

    @Select("select user_id from im_group_member where group_id = #{groupId} and quit = 0 ")
    List<Long> getUserIdsByGroupId(@Param("groupId") Long groupId);
}
