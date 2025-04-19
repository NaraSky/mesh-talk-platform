package com.lb.im.platform.friend.application.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.lb.im.common.cache.distribute.DistributedCacheService;
import com.lb.im.platform.common.exception.IMException;
import com.lb.im.platform.common.model.constants.IMPlatformConstants;
import com.lb.im.platform.common.model.entity.Friend;
import com.lb.im.platform.common.model.entity.User;
import com.lb.im.platform.common.model.enums.HttpCode;
import com.lb.im.platform.common.model.vo.FriendVO;
import com.lb.im.platform.common.session.SessionContext;
import com.lb.im.platform.dubbo.user.UserDubboService;
import com.lb.im.platform.friend.application.service.FriendService;
import com.lb.im.platform.friend.domain.model.command.FriendCommand;
import com.lb.im.platform.friend.domain.service.FriendDomainService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 好友应用服务实现类
 * 实现了好友模块对外提供的服务方法
 * 技术：
 * 1. 基于DDD的应用层设计
 * 2. 使用Redis进行分布式缓存
 * 3. 使用Dubbo进行RPC调用
 * 4. 使用Spring的事务管理
 * 5. 采用缓存策略模式（逻辑过期、穿透）
 */
@Service
public class FriendServiceImpl implements FriendService {

    private final Logger logger = LoggerFactory.getLogger(FriendServiceImpl.class);

    /**
     * 分布式缓存服务
     */
    @Autowired
    private DistributedCacheService distributedCacheService;

    /**
     * 好友领域服务
     */
    @Autowired
    private FriendDomainService domainService;

    /**
     * 用户Dubbo服务，用于远程调用用户服务
     */
    @DubboReference(version = IMPlatformConstants.DEFAULT_DUBBO_VERSION, check = false)
    private UserDubboService userDubboService;

    /**
     * 获取用户的好友ID列表
     * 使用逻辑过期策略的缓存
     * 
     * @param userId 用户ID
     * @return 好友ID列表
     */
    @Override
    public List<Long> getFriendIdList(Long userId) {
        return distributedCacheService.queryWithLogicalExpireList(
                IMPlatformConstants.PLATFORM_REDIS_FRIEND_LIST_KEY.concat(IMPlatformConstants.PLATFORM_REDIS_FRIEND_LIST_IDS),//platform:friend:list:ids:
                userId,
                Long.class,
                domainService::getFriendIdList,
                IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME,
                TimeUnit.MINUTES
        );
    }

    /**
     * 判断两个用户是否为好友关系
     * 先查Redis缓存，如果缓存未命中则查询数据库并更新缓存
     * 
     * @param userId1 用户ID1
     * @param userId2 用户ID2
     * @return 如果是好友返回true，否则返回false
     */
    @Override
    public Boolean isFriend(Long userId1, Long userId2) {
        // 构建Redis键
        String redisKey = IMPlatformConstants.PLATFORM_REDIS_FRIEND_SET_KEY.concat(String.valueOf(userId1));
        // 查询Redis缓存
        Boolean result = distributedCacheService.isMemberSet(redisKey, userId2);
        if (BooleanUtil.isTrue(result)) {
            return result;
        }
        // 缓存未命中，查询数据库
        result = domainService.isFriend(userId1, userId2);
        if (BooleanUtil.isTrue(result)) {
            // 更新缓存
            distributedCacheService.addSet(redisKey, String.valueOf(userId2));
        }
        return result;
    }

    /**
     * 根据用户ID查找其所有好友信息
     * 使用缓存穿透策略
     * 
     * @param userId 用户ID
     * @return 好友信息视图对象列表
     */
    @Override
    public List<FriendVO> findFriendByUserId(Long userId) {
        return distributedCacheService.queryWithPassThroughList(
                IMPlatformConstants.PLATFORM_REDIS_FRIEND_LIST_KEY.concat(IMPlatformConstants.PLATFORM_REDIS_FRIEND_LIST_USERVO),
                userId,
                FriendVO.class,
                domainService::findFriendByUserId,
                IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME,
                TimeUnit.MINUTES);
    }

    /**
     * 获取用户的所有好友实体对象
     * 使用缓存穿透策略
     * 
     * @param userId 用户ID
     * @return 好友实体对象列表
     */
    @Override
    public List<Friend> getFriendByUserId(Long userId) {
        return distributedCacheService.queryWithPassThroughList(
                IMPlatformConstants.PLATFORM_REDIS_FRIEND_LIST_KEY.concat(IMPlatformConstants.PLATFORM_REDIS_FRIEND_LIST_USER),
                Arrays.asList(userId),
                Friend.class,
                domainService::listByIds,
                IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME,
                TimeUnit.MINUTES
        );
    }

    /**
     * 添加好友关系
     * 注意：会同时建立双向的好友关系
     * 使用事务确保数据一致性
     * 
     * @param friendId 要添加的好友ID
     * @throws IMException 当尝试添加自己为好友时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFriend(Long friendId) {
        // 获取当前登录用户ID
        Long userId = SessionContext.getSession().getUserId();
        // 不允许添加自己为好友
        if (Objects.equals(userId, friendId)) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "不允许添加自己为好友");
        }
        // 创建好友命令对象
        FriendCommand friendCommand = new FriendCommand(userId, friendId);
        // 通过Dubbo调用获取好友用户信息
        User user = userDubboService.getUserById(friendId);
        // 建立好友关系（用户->好友）
        domainService.bindFriend(friendCommand, user == null ? "" : user.getHeadImage(), user == null ? "" : user.getNickName());

        // 创建反向好友命令对象
        friendCommand = new FriendCommand(friendId, userId);
        // 通过Dubbo调用获取当前用户信息
        user = userDubboService.getUserById(userId);
        // 建立好友关系（好友->用户）
        domainService.bindFriend(friendCommand, user == null ? "" : user.getHeadImage(), user == null ? "" : user.getNickName());
    }

    /**
     * 删除好友关系
     * 使用事务确保数据一致性
     * 
     * @param friendId 要删除的好友ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delFriend(Long friendId) {
        // 获取当前登录用户ID
        Long userId = SessionContext.getSession().getUserId();
        // 创建好友命令对象
        FriendCommand friendCommand = new FriendCommand(userId, friendId);
        // 解除好友关系
        domainService.unbindFriend(friendCommand);
        domainService.unbindFriend(friendCommand);
    }

    /**
     * 更新好友信息
     * 使用事务确保数据一致性
     * 
     * @param vo 好友信息视图对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(FriendVO vo) {
        domainService.update(vo, SessionContext.getSession().getUserId());
    }

    /**
     * 查找特定好友关系
     * 使用缓存穿透策略
     * 
     * @param friendId 好友ID
     * @return 好友信息视图对象
     */
    @Override
    public FriendVO findFriend(Long friendId) {
        return distributedCacheService.queryWithPassThrough(IMPlatformConstants.PLATFORM_REDIS_FRIEND_KEY,
                                                            new FriendCommand(SessionContext.getSession().getUserId(), friendId),
                                                            FriendVO.class,
                                                            domainService::findFriend,
                                                            IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME,
                                                            TimeUnit.MINUTES);
    }
}
