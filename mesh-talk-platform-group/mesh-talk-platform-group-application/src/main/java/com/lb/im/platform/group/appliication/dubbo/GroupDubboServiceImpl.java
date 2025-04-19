package com.lb.im.platform.group.appliication.dubbo;

import com.lb.im.platform.common.model.constants.IMPlatformConstants;
import com.lb.im.platform.common.model.entity.Group;
import com.lb.im.platform.common.model.params.GroupParams;
import com.lb.im.platform.common.model.vo.GroupMemberSimpleVO;
import com.lb.im.platform.dubbo.group.GroupDubboService;
import com.lb.im.platform.group.appliication.service.GroupService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@DubboService(version = IMPlatformConstants.DEFAULT_DUBBO_VERSION)
public class GroupDubboServiceImpl implements GroupDubboService {

    @Autowired
    private GroupService groupService;

    @Override
    public boolean isExists(Long groupId) {
        Group group = groupService.getById(groupId);
        if (Objects.isNull(group)) {
            return false;
        }
        if (group.getDeleted()) {
            return false;
        }
        return true;
    }

    @Override
    public GroupMemberSimpleVO getGroupMemberSimpleVO(GroupParams groupParams) {
        return groupService.getGroupMemberSimpleVO(groupParams);
    }

    @Override
    public List<Long> getUserIdsByGroupId(Long groupId) {
        return groupService.getUserIdsByGroupId(groupId);
    }

    @Override
    public List<Long> getGroupIdsByUserId(Long userId) {
        return groupService.getGroupIdsByUserId(userId);
    }

    @Override
    public List<GroupMemberSimpleVO> getGroupMemberSimpleVOList(Long userId) {
        return groupService.getGroupMemberSimpleVOList(userId);
    }
}
