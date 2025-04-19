package com.lb.im.platform.group.domain.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lb.im.platform.common.model.entity.Group;
import com.lb.im.platform.common.model.vo.GroupVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface GroupRepository extends BaseMapper<Group> {

    @Select("select owner_id from im_group where id = #{groupId}")
    Long getOwnerId(@Param("groupId") Long groupId);

    @Select("select g.id as id, g.name as name, g.owner_id as ownerId, g.head_image as headImage, " +
            "g.head_image_thumb as headImageThumb, g.notice as notice, gm.alias_name as aliasName, " +
            "gm.remark as remark from im_group g left join im_group_member gm on (g.id = gm.group_id) " +
            "where g.id = #{groupId} and gm.user_id = #{userId} ")
    GroupVO getGroupVOById(@Param("groupId") Long groupId, @Param("userId") Long userId);

    @Select("select g.id as id, g.name as name, g.owner_id as ownerId, g.head_image as headImage, " +
            "g.head_image_thumb as headImageThumb, g.notice as notice, gm.alias_name as aliasName, " +
            "gm.remark as remark from im_group g left join im_group_member gm on (g.id = gm.group_id) " +
            "where gm.user_id = #{userId} ")
    List<GroupVO> getGroupVOListByUserId(@Param("userId") Long userId);

    @Select("select name from im_group where id = #{groupId}")
    String getGroupName(@Param("groupId") Long groupId);
}
