package com.lb.im.platform.user.application.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.lb.im.common.cache.distribute.DistributedCacheService;
import com.lb.im.common.cache.id.SnowFlakeFactory;
import com.lb.im.common.domain.enums.IMDeviceType;
import com.lb.im.common.domain.jwt.JwtUtils;
import com.lb.im.platform.common.exception.IMException;
import com.lb.im.platform.common.jwt.JwtProperties;
import com.lb.im.platform.common.model.constants.IMPlatformConstants;
import com.lb.im.platform.common.model.dto.LoginDTO;
import com.lb.im.platform.common.model.dto.ModifyPwdDTO;
import com.lb.im.platform.common.model.dto.RegisterDTO;
import com.lb.im.platform.common.model.entity.User;
import com.lb.im.platform.common.model.enums.HttpCode;
import com.lb.im.platform.common.model.vo.LoginVO;
import com.lb.im.platform.common.model.vo.OnlineTerminalVO;
import com.lb.im.platform.common.model.vo.UserVO;
import com.lb.im.platform.common.session.SessionContext;
import com.lb.im.platform.common.session.UserSession;
import com.lb.im.platform.common.utils.BeanUtils;
import com.lb.im.platform.user.application.service.UserService;
import com.lb.im.platform.user.domain.service.UserDomainService;
import com.lb.im.sdk.client.IMClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户服务实现类，提供用户登录、注册等业务逻辑处理。
 * 通过缓存和数据库操作实现用户信息的查询与验证，并生成JWT令牌。
 */
@Service
public class UserServiceImpl implements UserService {

    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    @Autowired
    private UserDomainService userDomainService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private DistributedCacheService distributedCacheService;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private IMClient imClient;

    /**
     * 用户登录验证，验证成功后生成访问令牌和刷新令牌。
     *
     * @param dto 登录请求参数，包含用户名、密码及终端类型
     * @return 登录成功后的令牌信息，包含访问令牌、刷新令牌及其有效期
     */
    @Override
    public LoginVO login(LoginDTO dto) {
        if (dto == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        // 从缓存中查询用户信息（通过用户名）
        User user = distributedCacheService.queryWithPassThrough(
                IMPlatformConstants.PLATFORM_REDIS_USER_KEY,
                dto.getUserName(),
                User.class,
                userDomainService::getUserByUserName,
                IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME,
                TimeUnit.MINUTES);
        if (user == null) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "当前用户不存在");
        }
        // 验证密码是否正确
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IMException(HttpCode.PASSWORD_ERROR);
        }
        // 生成token部分
        UserSession session = BeanUtils.copyProperties(user, UserSession.class);
        session.setUserId(user.getId());
        session.setDeviceType(dto.getTerminal());
        String strJson = JSON.toJSONString(session);
        String accessToken = JwtUtils.sign(
                user.getId(),
                strJson,
                jwtProperties.getAccessTokenExpireIn(),
                jwtProperties.getAccessTokenSecret());
        String refreshToken = JwtUtils.sign(
                user.getId(),
                strJson,
                jwtProperties.getRefreshTokenExpireIn(),
                jwtProperties.getRefreshTokenSecret());
        LoginVO vo = new LoginVO();
        vo.setAccessToken(accessToken);
        vo.setAccessTokenExpiresIn(jwtProperties.getAccessTokenExpireIn());
        vo.setRefreshToken(refreshToken);
        vo.setRefreshTokenExpiresIn(jwtProperties.getRefreshTokenExpireIn());
        return vo;
    }

    /**
     * 用户注册功能，验证用户名唯一性后创建新用户。
     *
     * @param dto 注册请求参数，包含用户名、密码、昵称等信息
     */
    @Override
    public void register(RegisterDTO dto) {
        if (dto == null) {
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        // 检查用户名是否已被注册
        User user = distributedCacheService.queryWithPassThrough(
                IMPlatformConstants.PLATFORM_REDIS_USER_KEY,
                dto.getUserName(),
                User.class,
                userDomainService::getUserByUserName,
                IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME,
                TimeUnit.MINUTES);
        if (user != null) {
            throw new IMException(HttpCode.USERNAME_ALREADY_REGISTER);
        }
        // 创建用户对象并设置唯一ID、加密密码及创建时间
        user = BeanUtils.copyProperties(dto, User.class);
        user.setId(SnowFlakeFactory.getSnowFlakeFromCache().nextId());
        user.setCreatedTime(new Date());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 保存用户信息到数据库
        userDomainService.saveOrUpdateUser(user);
        logger.info("注册用户，用户id:{},用户名:{},昵称:{}", user.getId(), dto.getUserName(), dto.getNickName());
    }

    /**
     * 刷新访问令牌
     *
     * @param refreshToken 用户提供的刷新令牌
     * @return 包含新访问令牌和刷新令牌的登录信息对象
     * @throws IMException 当刷新令牌无效或已过期时抛出
     */
    @Override
    public LoginVO refreshToken(String refreshToken) throws IMException {
        // 验证 refreshToken 的签名和有效性
        if (!JwtUtils.checkSign(refreshToken, jwtProperties.getRefreshTokenSecret())) {
            throw new IMException("refreshToken无效或已过期");
        }

        String strJson = JwtUtils.getInfo(refreshToken);
        Long userId = JwtUtils.getUserId(refreshToken);

        // 生成新的访问令牌和刷新令牌
        String accessToken = JwtUtils.sign(userId, strJson, jwtProperties.getAccessTokenExpireIn(), jwtProperties.getAccessTokenSecret());
        String newRefreshToken = JwtUtils.sign(userId, strJson, jwtProperties.getRefreshTokenExpireIn(), jwtProperties.getRefreshTokenSecret());

        LoginVO vo = new LoginVO();
        vo.setAccessToken(accessToken);
        vo.setAccessTokenExpiresIn(jwtProperties.getAccessTokenExpireIn());
        vo.setRefreshToken(newRefreshToken);
        vo.setRefreshTokenExpiresIn(jwtProperties.getRefreshTokenExpireIn());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyPassword(ModifyPwdDTO dto) {
        //获取用户Session
        UserSession session = SessionContext.getSession();
        //不从缓存中获取，防止缓存数据不一致
        User user = userDomainService.getById(session.getUserId());
        if (user == null) {
            throw new IMException("用户不存在");
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new IMException("旧密码不正确");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userDomainService.saveOrUpdateUser(user);
        logger.info("用户修改密码，用户id:{},用户名:{},昵称:{}", user.getId(), user.getUserName(), user.getNickName());
    }

    @Override
    public User findUserByUserName(String username) {
        User user = distributedCacheService.queryWithPassThrough(IMPlatformConstants.PLATFORM_REDIS_USER_KEY, username, User.class, userDomainService::getUserByUserName, IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        if (user == null) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "当前用户不存在");
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UserVO vo) {
        UserSession session = SessionContext.getSession();
        if (!session.getUserId().equals(vo.getId())) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "不允许修改其他用户的信息!");
        }
        User user = userDomainService.getById(vo.getId());
        if (Objects.isNull(user)) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "用户不存在");
        }
        //TODO 如果用户更新了昵称和头像，则更新好友昵称和头像
        if (!user.getNickName().equals(vo.getNickName()) || !user.getHeadImageThumb().equals(vo.getHeadImageThumb())) {
            //TODO 后续完善
        }
        //TODO 群聊中的头像是缩略图，需要更新群聊中的头像
        if (!user.getNickName().equals(vo.getNickName()) || !user.getHeadImageThumb().equals(vo.getHeadImageThumb())) {

        }
        //更新用户的基本信息
        if (!StrUtil.isEmpty(vo.getNickName())) {
            user.setNickName(vo.getNickName());
        }
        if (vo.getSex() != null) {
            user.setSex(vo.getSex());
        }
        if (!StrUtil.isEmpty(vo.getSignature())) {
            user.setSignature(vo.getSignature());
        }
        if (!StrUtil.isEmpty(vo.getHeadImage())) {
            user.setHeadImage(vo.getHeadImage());
        }
        if (!StrUtil.isEmpty(vo.getHeadImageThumb())) {
            user.setHeadImageThumb(vo.getHeadImageThumb());
        }
        userDomainService.saveOrUpdateUser(user);
    }

    @Override
    public UserVO findUserById(Long id, boolean constantsOnlineFlag) {
        User user = distributedCacheService.queryWithPassThrough(IMPlatformConstants.PLATFORM_REDIS_USER_KEY, id, User.class, userDomainService::getById, IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        if (user == null) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "当前用户不存在");
        }
        UserVO vo = BeanUtils.copyProperties(user, UserVO.class);
        if (constantsOnlineFlag) {
            vo.setOnline(imClient.isOnline(id));
        }
        return vo;
    }

    @Override
    public List<UserVO> findUserByName(String name) {
        List<User> userList = userDomainService.findUserByName(name);
        //TODO 调用IMClient的方法后处理在线状态
        if (CollectionUtil.isEmpty(userList)) {
            return Collections.emptyList();
        }
        List<Long> userIds = userList.stream().map(User::getId).collect(Collectors.toList());
        List<Long> onlineUserIds = imClient.getOnlineUserList(userIds);
        return userList.stream().map(u -> {
            UserVO vo = BeanUtils.copyProperties(u, UserVO.class);
            vo.setOnline(onlineUserIds.contains(u.getId()));
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 根据用户ID列表获取在线终端信息。
     */
    @Override
    public List<OnlineTerminalVO> getOnlineTerminals(String userIds) {
        List<Long> userIdList = Arrays.stream(userIds.split(",")).map(Long::parseLong).collect(Collectors.toList());

        /**
         * 查询指定用户列表的在线终端信息。
         */
        Map<Long, List<IMDeviceType>> terminalMap = imClient.getOnlineTerminal(userIdList);

        /**
         * 将终端类型信息转换为视图对象。
         */
        List<OnlineTerminalVO> vos = new LinkedList<>();
        terminalMap.forEach((userId, types) -> {
            List<Integer> terminals = types.stream().map(IMDeviceType::getCode).collect(Collectors.toList());
            vos.add(new OnlineTerminalVO(userId, terminals));
        });
        return vos;
    }

}
