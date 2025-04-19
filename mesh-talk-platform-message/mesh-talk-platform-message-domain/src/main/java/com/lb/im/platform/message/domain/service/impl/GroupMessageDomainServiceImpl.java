package com.lb.im.platform.message.domain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lb.im.platform.common.model.entity.GroupMessage;
import com.lb.im.platform.message.domain.repository.GroupMessageRepository;
import com.lb.im.platform.message.domain.service.GroupMessageDomainService;
import org.springframework.stereotype.Service;

@Service
public class GroupMessageDomainServiceImpl extends ServiceImpl<GroupMessageRepository, GroupMessage> implements GroupMessageDomainService {
}
