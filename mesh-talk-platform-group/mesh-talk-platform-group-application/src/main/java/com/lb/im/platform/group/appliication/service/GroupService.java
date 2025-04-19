package com.lb.im.platform.group.appliication.service;

import com.lb.im.platform.common.model.entity.Group;
import com.lb.im.platform.common.model.vo.GroupInviteVO;
import com.lb.im.platform.common.model.vo.GroupMemberVO;
import com.lb.im.platform.common.model.vo.GroupVO;

import java.util.List;

public interface GroupService {

    GroupVO createGroup(GroupVO vo);

    GroupVO modifyGroup(GroupVO vo);

    void deleteGroup(Long groupId);

    void quitGroup(Long groupId);

    void kickGroup(Long groupId,Long userId);

    List<GroupVO> findGroups();

    void invite(GroupInviteVO vo);

    Group getById(Long groupId);

    GroupVO findById(Long groupId);

    List<GroupMemberVO> findGroupMembers(Long groupId);

    /**
     * 更新某个用户在所有群的头像
     */
    boolean updateHeadImgByUserId(String headImg, Long userId);
}
