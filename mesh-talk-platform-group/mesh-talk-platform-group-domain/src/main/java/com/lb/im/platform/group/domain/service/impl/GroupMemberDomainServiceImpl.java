package com.lb.im.platform.group.domain.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lb.im.platform.common.exception.IMException;
import com.lb.im.platform.common.model.entity.GroupMember;
import com.lb.im.platform.common.model.enums.HttpCode;
import com.lb.im.platform.common.model.vo.GroupMemberVO;
import com.lb.im.platform.group.domain.repository.GroupMemberRepository;
import com.lb.im.platform.group.domain.service.GroupMemberDomainService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupMemberDomainServiceImpl extends ServiceImpl<GroupMemberRepository, GroupMember> implements GroupMemberDomainService {

    @Override
    public boolean saveGroupMember(GroupMember groupMember) {
        if (groupMember == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        return baseMapper.insert(groupMember) > 0;
    }

    @Override
    public boolean saveGroupMemberList(List<GroupMember> groupMemberList) {
        if (CollectionUtil.isEmpty(groupMemberList)) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        return this.saveOrUpdateBatch(groupMemberList);
    }

    @Override
    public boolean updateGroupMember(GroupMember groupMember) {
        if (groupMember == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        return this.updateById(groupMember);
    }

    @Override
    public boolean removeMemberByGroupId(Long groupId) {
        if (groupId == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        LambdaUpdateWrapper<GroupMember> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(GroupMember::getGroupId, groupId)
                .set(GroupMember::getQuit, true);
        return this.update(wrapper);
    }

    @Override
    public boolean removeMember(Long userId, Long groupId) {
        if (userId == null || groupId == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        LambdaUpdateWrapper<GroupMember> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, userId)
                .set(GroupMember::getQuit, true);
        return this.update(wrapper);
    }

    @Override
    public GroupMember getGroupMemberByUserIdAndGroupId(Long userId, Long groupId) {
        if (userId == null || groupId == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        QueryWrapper<GroupMember> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, userId);
        return this.getOne(wrapper);
    }

    @Override
    public List<GroupMember> getGroupMemberListByGroupId(Long groupId) {
        LambdaQueryWrapper<GroupMember> memberWrapper = Wrappers.lambdaQuery();
        memberWrapper.eq(GroupMember::getGroupId, groupId);
        memberWrapper.eq(GroupMember::getQuit, false);
        return this.list(memberWrapper);
    }

    @Override
    public List<GroupMemberVO> getGroupMemberVoListByGroupId(Long groupId) {
        if (groupId == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        return baseMapper.getGroupMemberVoListByGroupId(groupId);
    }

    @Override
    public List<Long> getUserIdsByGroupId(Long groupId) {
        if (groupId == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        return baseMapper.getUserIdsByGroupId(groupId);
    }

    @Override
    public boolean updateHeadImgByUserId(String headImg, Long userId) {
        return baseMapper.updateHeadImgByUserId(headImg, userId) > 0;
    }
}
