package com.lb.im.platform.group.appliication.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.lb.im.common.cache.distribute.DistributedCacheService;
import com.lb.im.common.cache.id.SnowFlakeFactory;
import com.lb.im.common.domain.constans.IMConstants;
import com.lb.im.common.mq.event.MessageEventSenderService;
import com.lb.im.platform.common.exception.IMException;
import com.lb.im.platform.common.model.constants.IMPlatformConstants;
import com.lb.im.platform.common.model.entity.Friend;
import com.lb.im.platform.common.model.entity.Group;
import com.lb.im.platform.common.model.entity.GroupMember;
import com.lb.im.platform.common.model.entity.User;
import com.lb.im.platform.common.model.enums.HttpCode;
import com.lb.im.platform.common.model.vo.GroupInviteVO;
import com.lb.im.platform.common.model.vo.GroupMemberSimpleVO;
import com.lb.im.platform.common.model.vo.GroupMemberVO;
import com.lb.im.platform.common.model.vo.GroupVO;
import com.lb.im.platform.common.session.SessionContext;
import com.lb.im.platform.common.session.UserSession;
import com.lb.im.platform.dubbo.friend.FriendDubboService;
import com.lb.im.platform.dubbo.user.UserDubboService;
import com.lb.im.platform.group.appliication.service.GroupService;
import com.lb.im.platform.common.model.params.GroupParams;
import com.lb.im.platform.group.domain.event.IMGroupEvent;
import com.lb.im.platform.group.domain.service.GroupDomainService;
import com.lb.im.platform.group.domain.service.GroupMemberDomainService;
import com.lb.im.sdk.client.IMClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 群组服务实现类
 * 提供群组相关的业务逻辑实现，包括创建、修改、删除群组，管理群成员等功能
 * 使用分布式缓存和事件驱动架构提高系统性能和扩展性
 */
@Service
@CacheConfig(cacheNames = IMConstants.IM_CACHE_GROUP) // 指定缓存名称
public class GroupServiceImpl implements GroupService {
    // 日志记录器
    private final Logger logger = LoggerFactory.getLogger(GroupServiceImpl.class);


    // IM客户端，用于发送消息和检查用户在线状态
    @Autowired
    private IMClient imClient;

    // 事件类型配置，决定使用哪种消息队列实现
    @Value("${message.mq.event.type}")
    private String eventType;

    // 群组领域服务，处理核心群组业务逻辑
    @Autowired
    private GroupDomainService groupDomainService;

    // 分布式缓存服务，用于缓存群组和成员信息
    @Autowired
    private DistributedCacheService distributedCacheService;

    // 群成员领域服务，处理群成员相关业务逻辑
    @Autowired
    private GroupMemberDomainService groupMemberDomainService;

    // 用户Dubbo服务，用于跨服务获取用户信息
    @DubboReference(version = IMPlatformConstants.DEFAULT_DUBBO_VERSION, check = false)
    private UserDubboService userDubboService;

    // 好友Dubbo服务，用于跨服务获取好友关系
    @DubboReference(version = IMPlatformConstants.DEFAULT_DUBBO_VERSION, check = false)
    private FriendDubboService friendDubboService;

    // 消息事件发送服务，用于发布群组事件
    @Autowired
    private MessageEventSenderService messageEventSenderService;

    /**
     * 创建群组
     * 流程：创建群组基本信息，将创建者添加为群成员，发送群组创建事件
     *
     * @param vo 群组信息对象
     * @return 创建成功的群组信息
     * @throws IMException 参数错误或用户不存在时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 开启事务管理，任何异常都回滚
    public GroupVO createGroup(GroupVO vo) {
        // 参数校验
        if (vo == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }

        // 获取当前用户会话信息
        UserSession session = SessionContext.getSession();

        // 获取用户信息
        User user = userDubboService.getUserById(session.getUserId());
        if (user == null) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "未获取到用户信息");
        }

        // 创建群组并获取群组信息
        vo = this.getGroupVO(groupDomainService.createGroup(vo, session.getUserId()), session, user);
        logger.info("创建群聊，群聊id:{},群聊名称:{}", vo.getId(), vo.getName());

        // 发送群组创建事件
        IMGroupEvent imGroupEvent = new IMGroupEvent(
                vo.getId(),
                user.getId(),
                IMPlatformConstants.GROUP_HANDLER_CREATE,
                this.getTopicEvent()
        );
        messageEventSenderService.send(imGroupEvent);

        return vo;
    }

    /**
     * 构建群组VO对象并添加创建者作为群成员
     *
     * @param vo      群组基本信息
     * @param session 用户会话信息
     * @param user    用户实体对象
     * @return 完整的群组VO对象
     */
    private GroupVO getGroupVO(GroupVO vo, UserSession session, User user) {
        // 把群主加入群组
        GroupMember groupMember = new GroupMember();
        groupMember.setId(SnowFlakeFactory.getSnowFlakeFromCache().nextId()); // 生成唯一ID
        groupMember.setGroupId(vo.getId());
        groupMember.setUserId(user.getId());
        groupMember.setHeadImage(user.getHeadImageThumb());
        groupMember.setAliasName(org.apache.commons.lang3.StringUtils.isEmpty(vo.getAliasName()) ? session.getNickName() : vo.getAliasName());
        groupMember.setRemark(vo.getRemark());
        groupMember.setCreatedTime(new Date());
        groupMemberDomainService.save(groupMember);

        // 设置群组VO的别名和备注
        vo.setAliasName(groupMember.getAliasName());
        vo.setRemark(groupMember.getRemark());
        return vo;
    }

    /**
     * 修改群组信息
     * 更新群组基本信息和当前用户在群中的别名和备注
     *
     * @param vo 群组信息对象
     * @return 更新后的群组信息
     * @throws IMException 参数错误或用户不是群成员时抛出异常
     */
    @Override
    @CacheEvict(value = "#vo.getId()") // 清除指定群组的缓存
    @Transactional(rollbackFor = Exception.class)
    public GroupVO modifyGroup(GroupVO vo) {
        // 参数校验
        if (vo == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }

        // 获取当前用户会话
        UserSession session = SessionContext.getSession();

        // 更新群组信息
        vo = groupDomainService.modifyGroup(vo, session.getUserId());

        // 获取用户在群中的成员信息
        GroupMember groupMember = groupMemberDomainService.getGroupMemberByUserIdAndGroupId(session.getUserId(), vo.getId());
        if (groupMember == null) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "您不是群聊的成员");
        }

        // 更新群成员信息
        groupMember.setAliasName(StringUtils.isEmpty(vo.getAliasName()) ? session.getNickName() : vo.getAliasName());
        groupMember.setRemark(vo.getRemark());

        // 保存更新并发送事件
        if (groupMemberDomainService.updateGroupMember(groupMember)) {
            logger.info("修改群聊，群聊id:{},群聊名称:{}", vo.getId(), vo.getName());

            // 发送群组修改事件
            IMGroupEvent imGroupEvent = new IMGroupEvent(
                    vo.getId(),
                    session.getUserId(),
                    IMPlatformConstants.GROUP_HANDLER_MODIFY,
                    this.getTopicEvent()
            );
            messageEventSenderService.send(imGroupEvent);
        }

        return vo;
    }

    /**
     * 删除群组
     * 标记群组和群成员为已删除状态
     *
     * @param groupId 群组ID
     * @throws IMException 参数错误时抛出异常
     */
    @Override
    @CacheEvict(value = "#groupId") // 清除指定群组的缓存
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroup(Long groupId) {
        // 参数校验
        if (groupId == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }

        // 获取当前用户会话
        UserSession session = SessionContext.getSession();

        // 标记删除群组
        boolean result = groupDomainService.deleteGroup(groupId, session.getUserId());

        // 群组标记删除成功
        if (result) {
            // 删除群成员
            groupMemberDomainService.removeMemberByGroupId(groupId);
            logger.info("删除群聊，群聊id:{}", groupId);

            // 发送群组删除事件
            IMGroupEvent imGroupEvent = new IMGroupEvent(
                    groupId,
                    session.getUserId(),
                    IMPlatformConstants.GROUP_HANDLER_DELETE,
                    this.getTopicEvent()
            );
            messageEventSenderService.send(imGroupEvent);
        } else {
            logger.info("删除群聊失败");
        }
    }

    /**
     * 退出群组
     * 将用户从群组中移除
     *
     * @param groupId 群组ID
     * @throws IMException 参数错误或不允许退出时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void quitGroup(Long groupId) {
        // 参数校验
        if (groupId == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }

        // 获取当前用户会话
        UserSession session = SessionContext.getSession();

        // 验证是否可执行退群操作
        if (!groupDomainService.quitGroup(groupId, session.getUserId())) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "不可退出群组");
        }

        // 移除群成员
        if (groupMemberDomainService.removeMember(session.getUserId(), groupId)) {
            logger.info("用户退群成功，用户id:{}, 群id:{}", session.getUserId(), groupId);

            // 发送退群事件
            IMGroupEvent imGroupEvent = new IMGroupEvent(
                    groupId,
                    session.getUserId(),
                    IMPlatformConstants.GROUP_HANDLER_QUIT,
                    this.getTopicEvent()
            );
            messageEventSenderService.send(imGroupEvent);
        }
    }

    /**
     * 踢出群组成员
     * 群主将指定用户踢出群组
     *
     * @param groupId 群组ID
     * @param userId  被踢出用户ID
     * @throws IMException 参数错误或权限不足时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void kickGroup(Long groupId, Long userId) {
        // 参数校验
        if (groupId == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }

        // 获取当前用户会话
        UserSession session = SessionContext.getSession();

        // 验证踢人权限
        if (!groupDomainService.kickGroup(groupId, userId, session.getUserId())) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "踢人异常");
        }

        // 移除群成员
        if (groupMemberDomainService.removeMember(userId, groupId)) {
            logger.info("群主踢人成功，群主id:{}, 群成员id:{}, 群id:{}", session.getUserId(), userId, groupId);

            // 发送踢人事件
            IMGroupEvent imGroupEvent = new IMGroupEvent(
                    groupId,
                    session.getUserId(),
                    IMPlatformConstants.GROUP_HANDLER_KICK,
                    this.getTopicEvent()
            );
            messageEventSenderService.send(imGroupEvent);
        }
    }

    /**
     * 查询当前用户所在的所有群组
     * 使用分布式缓存提高查询性能
     *
     * @return 群组信息列表
     */
    @Override
    public List<GroupVO> findGroups() {
        // 从缓存获取数据，缓存未命中时从数据库加载
        return distributedCacheService.queryWithPassThroughList(
                IMPlatformConstants.PLATFORM_REDIS_GROUP_LIST_KEY,
                SessionContext.getSession().getUserId(),
                GroupVO.class,
                groupDomainService::getGroupVOListByUserId,
                IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME,
                TimeUnit.MINUTES
        );
    }

    /**
     * 邀请好友加入群组
     * 将多个好友一次性添加到群组中
     *
     * @param vo 邀请参数，包含群组ID和好友ID列表
     * @throws IMException 参数错误、群不存在或好友关系不存在时抛出异常
     */
    @Override
    public void invite(GroupInviteVO vo) {
        // 参数校验
        if (vo == null || CollectionUtil.isEmpty(vo.getFriendIds())) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }

        // 获取群组名称
        String groupName = groupDomainService.getGroupName(vo.getGroupId());
        if (StrUtil.isEmpty(groupName)) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "群聊不存在");
        }

        // 获取群组现有群成员
        List<GroupMember> members = groupMemberDomainService.getGroupMemberListByGroupId(vo.getGroupId());

        // 计算现有成员数量
        long size = CollectionUtil.isEmpty(members) ? 0 : members.size();

        // 检查群成员数量上限
        if (vo.getFriendIds().size() + size > IMConstants.MAX_GROUP_MEMBER) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "群聊人数不能大于" + IMConstants.MAX_GROUP_MEMBER + "人");
        }

        // 获取当前用户会话
        UserSession session = SessionContext.getSession();

        // 获取当前用户的好友列表
        List<Friend> friendList = friendDubboService.getFriendByUserId(session.getUserId());
        if (friendList == null) {
            friendList = Collections.emptyList();
        }

        // 过滤出要邀请的好友
        List<Friend> finalFriendList = friendList;
        List<Friend> userFriendList = vo.getFriendIds().stream()
                .map(id -> finalFriendList.stream()
                        .filter(f -> f.getFriendId().equals(id))
                        .findFirst().get())
                .collect(Collectors.toList());

        // 验证邀请的用户都是自己的好友
        if (finalFriendList.size() != vo.getFriendIds().size()) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "部分用户不是您的好友，邀请失败");
        }

        // 保存群成员并发送事件
        if (groupMemberDomainService.saveGroupMemberList(this.getGroupMemberList(vo, groupName, members, userFriendList))) {
            logger.info("邀请进入群聊，群聊id:{},群聊名称:{},被邀请用户id:{}", vo.getGroupId(), groupName, vo.getFriendIds());

            // 发送邀请事件
            IMGroupEvent imGroupEvent = new IMGroupEvent(
                    vo.getGroupId(),
                    session.getUserId(),
                    IMPlatformConstants.GROUP_HANDLER_INVITE,
                    this.getTopicEvent()
            );
            messageEventSenderService.send(imGroupEvent);
        }
    }

    /**
     * 构建群成员列表
     * 将好友信息转换为群成员对象
     *
     * @param vo             邀请参数
     * @param groupName      群组名称
     * @param members        现有群成员列表
     * @param userFriendList 好友列表
     * @return 待添加的群成员列表
     */
    private List<GroupMember> getGroupMemberList(GroupInviteVO vo, String groupName, List<GroupMember> members, List<Friend> userFriendList) {
        return userFriendList.stream().map(f -> {
            // 检查是否已经是群成员
            Optional<GroupMember> optional = members.stream()
                    .filter(m -> m.getUserId().equals(f.getFriendId()))
                    .findFirst();

            // 创建或获取群成员对象
            GroupMember groupMember = optional.orElseGet(GroupMember::new);
            groupMember.setId(SnowFlakeFactory.getSnowFlakeFromCache().nextId());
            groupMember.setGroupId(vo.getGroupId());
            groupMember.setUserId(f.getFriendId());
            groupMember.setAliasName(f.getFriendNickName());
            groupMember.setRemark(groupName);
            groupMember.setHeadImage(f.getFriendHeadImage());
            groupMember.setCreatedTime(new Date());
            groupMember.setQuit(false);
            return groupMember;
        }).collect(Collectors.toList());
    }

    /**
     * 根据ID获取群组信息
     * 使用缓存优化查询性能
     *
     * @param groupId 群组ID
     * @return 群组实体
     * @throws IMException 参数错误时抛出异常
     */
    @Override
    @Cacheable(value = "#groupId") // 缓存查询结果
    public Group getById(Long groupId) {
        if (groupId == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }

        // 从缓存获取数据，缓存未命中时从数据库加载
        return distributedCacheService.queryWithPassThrough(
                IMPlatformConstants.PLATFORM_REDIS_GROUP_SINGLE_KEY,
                groupId,
                Group.class,
                groupDomainService::getGroupById,
                IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME,
                TimeUnit.MINUTES
        );
    }

    /**
     * 查询当前用户在指定群组中的详细信息
     *
     * @param groupId 群组ID
     * @return 群组VO，包含用户在群中的信息
     * @throws IMException 参数错误或用户不在群中时抛出异常
     */
    @Override
    public GroupVO findById(Long groupId) {
        if (groupId == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }

        // 构建查询参数
        GroupParams params = new GroupParams(SessionContext.getSession().getUserId(), groupId);

        // 从缓存获取数据
        GroupVO groupVO = distributedCacheService.queryWithPassThrough(
                IMPlatformConstants.PLATFORM_REDIS_GROUP_VO_SINGLE_KEY,
                params,
                GroupVO.class,
                groupDomainService::getGroupVOByParams,
                IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME,
                TimeUnit.MINUTES
        );

        // 验证用户是否在群中
        if (groupVO == null) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "您未加入群聊");
        }

        return groupVO;
    }

    /**
     * 查询群组成员列表
     * 包含成员的在线状态信息
     *
     * @param groupId 群组ID
     * @return 群组成员VO列表
     * @throws IMException 参数错误时抛出异常
     */
    @Override
    public List<GroupMemberVO> findGroupMembers(Long groupId) {
        if (groupId == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }

        // 从缓存获取数据
        return distributedCacheService.queryWithPassThroughList(
                IMPlatformConstants.PLATFORM_REDIS_MEMBER_VO_LIST_KEY,
                groupId,
                GroupMemberVO.class,
                this::getGroupMemberVOS,
                IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME,
                TimeUnit.MINUTES
        );
    }

    /**
     * 获取用户在群组中的简要信息
     *
     * @param groupParams 包含用户ID和群组ID的参数对象
     * @return 群组成员简要信息
     * @throws IMException 参数错误时抛出异常
     */
    @Override
    public GroupMemberSimpleVO getGroupMemberSimpleVO(GroupParams groupParams) {
        if (groupParams == null || groupParams.isEmpty()) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }

        // 从缓存获取数据
        return distributedCacheService.queryWithPassThrough(
                IMPlatformConstants.PLATFORM_REDIS_MEMBER_VO_SIMPLE_KEY,
                groupParams,
                GroupMemberSimpleVO.class,
                groupMemberDomainService::getGroupMemberSimpleVO,
                IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME,
                TimeUnit.MINUTES
        );
    }

    /**
     * 获取群组成员ID列表
     *
     * @param groupId 群组ID
     * @return 成员ID列表
     * @throws IMException 参数错误时抛出异常
     */
    @Override
    public List<Long> getUserIdsByGroupId(Long groupId) {
        if (groupId == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }

        // 从缓存获取数据
        return distributedCacheService.queryWithPassThroughList(
                IMPlatformConstants.PLATFORM_REDIS_MEMBER_ID_KEY,
                groupId,
                Long.class,
                groupMemberDomainService::getUserIdsByGroupId,
                IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME,
                TimeUnit.MINUTES
        );
    }

    /**
     * 获取用户所在的所有群组ID列表
     *
     * @param userId 用户ID
     * @return 群组ID列表
     */
    @Override
    public List<Long> getGroupIdsByUserId(Long userId) {
        // 获取用户在各群组中的信息
        List<GroupMemberSimpleVO> list = this.getGroupMemberSimpleVOList(userId);

        if (CollectionUtil.isEmpty(list)) {
            return Collections.emptyList();
        }

        // 提取群组ID
        return list.stream()
                .map(GroupMemberSimpleVO::getGroupId)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户在所有群组中的简要信息列表
     *
     * @param userId 用户ID
     * @return 简要信息列表
     * @throws IMException 参数错误时抛出异常
     */
    @Override
    public List<GroupMemberSimpleVO> getGroupMemberSimpleVOList(Long userId) {
        if (userId == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }

        // 从缓存获取数据
        return distributedCacheService.queryWithPassThroughList(
                IMPlatformConstants.PLATFORM_REDIS_MEMBER_LIST_SIMPLE_KEY,
                userId,
                GroupMemberSimpleVO.class,
                groupMemberDomainService::getGroupMemberSimpleVOList,
                IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME,
                TimeUnit.MINUTES
        );
    }

    /**
     * 更新用户在所有群组中的头像
     *
     * @param headImg 头像URL
     * @param userId  用户ID
     * @return 是否更新成功
     */
    @Override
    public boolean updateHeadImgByUserId(String headImg, Long userId) {
        return groupMemberDomainService.updateHeadImgByUserId(headImg, userId);
    }

    /**
     * 获取群组成员列表并设置在线状态
     *
     * @param groupId 群组ID
     * @return 带在线状态的群组成员列表
     */
    @NotNull
    private List<GroupMemberVO> getGroupMemberVOS(Long groupId) {
        // 获取群组成员列表
        List<GroupMemberVO> memberList = groupMemberDomainService.getGroupMemberVoListByGroupId(groupId);

        // 提取所有成员ID
        List<Long> userList = memberList.stream()
                .map(GroupMemberVO::getUserId)
                .collect(Collectors.toList());

        // 获取在线用户ID列表
        List<Long> onlineUserIdList = imClient.getOnlineUserList(userList);

        // 设置在线状态并按在线状态排序
        return memberList.stream()
                .peek(m -> m.setOnline(onlineUserIdList.contains(m.getUserId())))
                .sorted((m1, m2) -> m2.getOnline().compareTo(m1.getOnline()))
                .collect(Collectors.toList());
    }

    /**
     * 获取事件主题
     * 根据配置决定使用RocketMQ还是Cola框架
     *
     * @return 事件主题名称
     */
    private String getTopicEvent() {
        return IMPlatformConstants.EVENT_PUBLISH_TYPE_ROCKETMQ.equals(eventType)
                ? IMPlatformConstants.TOPIC_EVENT_ROCKETMQ_GROUP
                : IMPlatformConstants.TOPIC_EVENT_COLA;
    }
}