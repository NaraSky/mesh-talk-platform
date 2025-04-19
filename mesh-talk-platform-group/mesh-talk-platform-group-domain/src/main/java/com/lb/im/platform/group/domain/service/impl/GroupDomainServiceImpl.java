package com.lb.im.platform.group.domain.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lb.im.common.cache.id.SnowFlakeFactory;
import com.lb.im.platform.common.exception.IMException;
import com.lb.im.platform.common.model.entity.Group;
import com.lb.im.platform.common.model.enums.HttpCode;
import com.lb.im.platform.common.model.vo.GroupVO;
import com.lb.im.platform.common.utils.BeanUtils;
import com.lb.im.platform.group.domain.command.GroupParams;
import com.lb.im.platform.group.domain.repository.GroupMemberRepository;
import com.lb.im.platform.group.domain.repository.GroupRepository;
import com.lb.im.platform.group.domain.service.GroupDomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class GroupDomainServiceImpl extends ServiceImpl<GroupRepository, Group> implements GroupDomainService {

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Override
    public GroupVO createGroup(GroupVO vo, Long userId) {
        if (vo == null || userId == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        //保存群信息
        vo.setId(SnowFlakeFactory.getSnowFlakeFromCache().nextId());
        Group group = BeanUtils.copyProperties(vo, Group.class);
        //设置群主id
        group.setOwnerId(userId);
        group.setCreatedTime(new Date());
        int count = baseMapper.insert(group);
        if (count <= 0) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "创建群失败");
        }
        vo.setRemark(StrUtil.isEmpty(group.getName()) ? vo.getRemark() : group.getName());
        return vo;
    }

    @Override
    public GroupVO modifyGroup(GroupVO vo, Long userId) {
        if (vo == null || userId == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        Group group = this.getGroupById(vo.getId());
        if (group == null) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "群不存在");
        }
        //只有群主才能更新群信息
        if (group.getOwnerId().equals(userId)) {
            group = BeanUtils.copyProperties(vo, Group.class);
            this.updateById(group);
        }
        vo.setRemark(StrUtil.isEmpty(vo.getRemark()) ? group.getName() : vo.getRemark());
        return vo;
    }

    @Override
    public boolean deleteGroup(Long groupId, Long userId) {
        if (groupId == null || userId == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        Group group = this.getGroupById(groupId);
        if (group == null) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "群不存在");
        }
        if (!group.getOwnerId().equals(userId)) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "只有群主才有权限解除群聊");
        }
        group.setDeleted(true);
        return this.updateById(group);
    }

    @Override
    public boolean quitGroup(Long groupId, Long userId) {
        if (groupId == null || userId == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        Long ownerId = baseMapper.getOwnerId(groupId);
        if (ownerId == null) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "群不存在");
        }
        if (ownerId.equals(userId)) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "您是群主，不可退出群聊");
        }
        return true;
    }

    @Override
    public boolean kickGroup(Long groupId, Long kickUserId, Long userId) {
        if (groupId == null || kickUserId == null || userId == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        Long ownerId = baseMapper.getOwnerId(groupId);
        if (ownerId == null) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "群不存在");
        }
        if (!ownerId.equals(userId)) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "您不是群主，没有权限踢人");
        }
        if (kickUserId.equals(userId)) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "不能自己踢自己");
        }
        return true;
    }

    @Override
    public GroupVO getGroupVOByParams(GroupParams params) {
        if (params == null || params.isEmpty()){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        return baseMapper.getGroupVOById(params.getGroupId(), params.getUserId());
    }

    @Override
    public GroupVO getGroupVOById(Long groupId, Long userId) {
        if (groupId == null || userId == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        return baseMapper.getGroupVOById(groupId, userId);
    }

    @Override
    public Group getGroupById(Long groupId) {
        Group group = this.getById(groupId);
        if (group == null) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "群组不存在");
        }
        if (group.getDeleted()) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "群组'" + group.getName() + "'已解散");
        }
        return group;
    }

    @Override
    public List<GroupVO> getGroupVOListByUserId(Long userId) {
        if (userId == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        return baseMapper.getGroupVOListByUserId(userId);
    }

    @Override
    public String getGroupName(Long groupId) {
        return baseMapper.getGroupName(groupId);
    }
}
