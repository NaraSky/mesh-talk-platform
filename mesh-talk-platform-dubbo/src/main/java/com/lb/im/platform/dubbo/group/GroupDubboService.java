package com.lb.im.platform.dubbo.group;


import com.lb.im.platform.common.model.vo.GroupMemberSimpleVO;
import org.redisson.api.search.aggregate.GroupParams;

import java.util.List;

public interface GroupDubboService {

    /**
     * 检测群是否存在
     */
    boolean isExists(Long groupId);

    /**
     * 获取成员
     */
    GroupMemberSimpleVO getGroupMemberSimpleVO(GroupParams groupParams);

    /**
     * 获取群成员id列表
     */
    List<Long> getUserIdsByGroupId(Long groupId);

    /**
     * 根据用户id获取群组id列表
     */
    List<Long> getGroupIdsByUserId(Long userId);

    /**
     * 根据用户id获取在各个群组中的信息
     */
    List<GroupMemberSimpleVO> getGroupMemberSimpleVOList(Long userId);
}
