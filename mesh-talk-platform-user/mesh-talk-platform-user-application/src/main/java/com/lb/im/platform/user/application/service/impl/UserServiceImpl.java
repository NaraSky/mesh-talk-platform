package com.lb.im.platform.user.application.service.impl;

import com.alibaba.fastjson.JSON;
import com.lb.im.common.cache.distribute.DistributedCacheService;
import com.lb.im.common.cache.id.SnowFlakeFactory;
import com.lb.im.common.domain.jwt.JwtUtils;
import com.lb.im.platform.common.exception.IMException;
import com.lb.im.platform.common.jwt.JwtProperties;
import com.lb.im.platform.common.model.constants.IMPlatformConstants;
import com.lb.im.platform.common.model.dto.LoginDTO;
import com.lb.im.platform.common.model.dto.RegisterDTO;
import com.lb.im.platform.common.model.entity.User;
import com.lb.im.platform.common.model.enums.HttpCode;
import com.lb.im.platform.common.model.vo.LoginVO;
import com.lb.im.platform.common.session.UserSession;
import com.lb.im.platform.common.utils.BeanUtils;
import com.lb.im.platform.user.application.service.UserService;
import com.lb.im.platform.user.domain.service.UserDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

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

}
