package com.lb.im.platform.friend.application.service.impl;

import cn.hutool.core.collection.CollectionUtil;
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
import java.util.stream.Collectors;

/**
 * 好友应用服务实现类
 * 实现了好友模块对外提供的服务方法
 * <p>
 * 技术点：
 * 1. 基于DDD的应用层设计，协调领域层和基础设施层
 * 2. 使用Redis进行分布式缓存，实现数据快速访问
 * 3. 使用Dubbo进行RPC调用，实现跨服务通信
 * 4. 使用Spring的事务管理，确保数据一致性
 * 5. 采用多种缓存策略（缓存穿透、缓存击穿防护）
 * 6. 使用Java 8 Stream API进行集合数据处理
 */
@Service
public class FriendServiceImpl implements FriendService {

    private final Logger logger = LoggerFactory.getLogger(FriendServiceImpl.class);

    /**
     * 分布式缓存服务
     * 用于管理Redis缓存操作，提高数据访问性能
     */
    @Autowired
    private DistributedCacheService distributedCacheService;

    /**
     * 好友领域服务
     * 封装了好友关系的核心业务逻辑
     */
    @Autowired
    private FriendDomainService domainService;

    /**
     * 用户Dubbo服务
     * 通过RPC远程调用用户服务，获取用户信息
     */
    @DubboReference(version = IMPlatformConstants.DEFAULT_DUBBO_VERSION, check = false)
    private UserDubboService userDubboService;

    /**
     * 先从缓存获取好友列表，然后提取好友ID
     *
     * @param userId 用户ID
     * @return 好友ID列表
     * @throws IMException 当参数错误时抛出异常
     */
    @Override
    public List<Long> getFriendIdList(Long userId) {
        // 参数校验
        if (userId == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        // 获取好友列表
        List<Friend> friendList = this.getFriendByUserId(userId);
        // 如果列表为空，返回空集合
        if (CollectionUtil.isEmpty(friendList)) {
            return Collections.emptyList();
        }
        // 使用Stream API提取好友ID
        return friendList.stream().map(Friend::getFriendId).collect(Collectors.toList());
    }

    /**
     * 判断两个用户是否为好友关系
     * 先查Redis缓存，如果缓存未命中则查询数据库并更新缓存
     *
     * @param userId1 用户ID1
     * @param userId2 用户ID2
     * @return 如果是好友返回true，否则返回false
     * @throws IMException 当参数错误时抛出异常
     */
    @Override
    public Boolean isFriend(Long userId1, Long userId2) {
        // 参数校验
        if (userId1 == null || userId2 == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        // 构建Redis键
        String redisKey = IMPlatformConstants.PLATFORM_REDIS_FRIEND_SET_KEY.concat(String.valueOf(userId1));
        // 查询Redis缓存（使用Set数据结构存储好友关系）
        Boolean result = distributedCacheService.isMemberSet(redisKey, String.valueOf(userId2));
        // 如果缓存命中且为true，直接返回结果
        if (BooleanUtil.isTrue(result)) {
            return result;
        }
        // 缓存未命中或为false，查询数据库
        result = domainService.isFriend(userId1, userId2);
        // 如果是好友关系，更新缓存
        if (BooleanUtil.isTrue(result)) {
            distributedCacheService.addSet(redisKey, String.valueOf(userId2));
        }
        return result;
    }

    /**
     * 根据用户ID查找其所有好友信息
     * 先从缓存获取好友列表，然后转换为VO对象
     *
     * @param userId 用户ID
     * @return 好友信息视图对象列表
     * @throws IMException 当参数错误时抛出异常
     */
    @Override
    public List<FriendVO> findFriendByUserId(Long userId) {
        // 参数校验
        if (userId == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        // 获取好友列表
        List<Friend> friendList = this.getFriendByUserId(userId);
        // 如果列表为空，返回空集合
        if (CollectionUtil.isEmpty(friendList)) {
            return Collections.emptyList();
        }
        // 使用Stream API将Friend实体转换为FriendVO
        return friendList.stream()
                .map(friend -> new FriendVO(friend.getFriendId(), friend.getFriendNickName(), friend.getFriendHeadImage()))
                .collect(Collectors.toList());
    }

    /**
     * 获取用户的所有好友实体对象
     * 使用缓存穿透策略，避免缓存穿透问题
     *
     * @param userId 用户ID
     * @return 好友实体对象列表
     * @throws IMException 当参数错误时抛出异常
     */
    @Override
    public List<Friend> getFriendByUserId(Long userId) {
        // 参数校验
        if (userId == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        // 使用缓存穿透策略查询数据
        // 如果缓存中有数据，直接返回；如果没有，则查询数据库并缓存结果
        return distributedCacheService.queryWithPassThroughList(
                IMPlatformConstants.PLATFORM_REDIS_FRIEND_LIST_KEY, // 缓存键前缀
                userId, // 缓存键值
                Friend.class, // 返回类型
                domainService::getFriendByUserId, // 数据库查询函数
                IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME, // 缓存过期时间
                TimeUnit.MINUTES // 时间单位
        );
    }

    /**
     * 添加好友关系
     * 注意：会同时建立双向的好友关系，即A->B和B->A
     * 使用事务确保数据一致性
     *
     * @param friendId 要添加的好友ID
     * @throws IMException 当参数错误或尝试添加自己为好友时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 使用事务确保数据一致性
    public void addFriend(Long friendId) {
        // 参数校验
        if (friendId == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        // 获取当前登录用户ID
        Long userId = SessionContext.getSession().getUserId();
        // 不允许添加自己为好友
        if (Objects.equals(userId, friendId)) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "不允许添加自己为好友");
        }

        // 第一步：建立用户->好友的关系
        // 创建好友命令对象
        FriendCommand friendCommand = new FriendCommand(userId, friendId);
        // 通过Dubbo调用获取好友用户信息
        User user = userDubboService.getUserById(friendId);
        // 建立好友关系（用户->好友）
        domainService.bindFriend(friendCommand,
                                 user == null ? "" : user.getHeadImage(),
                                 user == null ? "" : user.getNickName());

        // 第二步：建立好友->用户的关系（双向好友关系）
        // 创建反向好友命令对象
        friendCommand = new FriendCommand(friendId, userId);
        // 通过Dubbo调用获取当前用户信息
        user = userDubboService.getUserById(userId);
        // 建立好友关系（好友->用户）
        domainService.bindFriend(friendCommand,
                                 user == null ? "" : user.getHeadImage(),
                                 user == null ? "" : user.getNickName());
    }

    /**
     * 删除好友关系
     * 同时删除双向的好友关系
     * 使用事务确保数据一致性
     *
     * @param friendId 要删除的好友ID
     * @throws IMException 当参数错误时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 使用事务确保数据一致性
    public void delFriend(Long friendId) {
        // 参数校验
        if (friendId == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        // 获取当前登录用户ID
        Long userId = SessionContext.getSession().getUserId();

        // 第一步：解除用户->好友的关系
        FriendCommand friendCommand = new FriendCommand(userId, friendId);
        domainService.unbindFriend(friendCommand);

        // 第二步：解除好友->用户的关系
        friendCommand = new FriendCommand(friendId, userId);
        domainService.unbindFriend(friendCommand);
    }

    /**
     * 更新好友信息
     * 修改好友的昵称、头像等信息
     * 使用事务确保数据一致性
     *
     * @param vo 好友信息视图对象
     * @throws IMException 当参数错误时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 使用事务确保数据一致性
    public void update(FriendVO vo) {
        // 参数校验
        if (vo == null || vo.getId() == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        // 更新好友信息
        domainService.update(vo, SessionContext.getSession().getUserId());
    }

    /**
     * 查找特定好友关系
     * 使用缓存穿透策略，避免缓存穿透问题
     *
     * @param friendId 好友ID
     * @return 好友信息视图对象
     * @throws IMException 当参数错误时抛出异常
     */
    @Override
    public FriendVO findFriend(Long friendId) {
        // 参数校验
        if (friendId == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        // 使用缓存穿透策略查询数据
        return distributedCacheService.queryWithPassThrough(
                IMPlatformConstants.PLATFORM_REDIS_FRIEND_SINGLE_KEY, // 缓存键前缀
                new FriendCommand(SessionContext.getSession().getUserId(), friendId), // 缓存键值
                FriendVO.class, // 返回类型
                domainService::findFriend, // 数据库查询函数
                IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME, // 缓存过期时间
                TimeUnit.MINUTES // 时间单位
        );
    }
}
