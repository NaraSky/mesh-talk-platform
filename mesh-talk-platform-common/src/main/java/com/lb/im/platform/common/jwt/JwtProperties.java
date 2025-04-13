package com.lb.im.platform.common.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JwtProperties类用于存储和管理JWT（JSON Web Token）相关的配置属性，包括访问令牌和刷新令牌的过期时间和密钥。该类通过Spring的@Component注解被注册为Bean，属性值通过@Value从配置文件中注入。
 */
@Component
public class JwtProperties {

    @Value("${jwt.accessToken.expireIn}")
    private Integer accessTokenExpireIn;

    @Value("${jwt.accessToken.secret}")
    private String accessTokenSecret;

    @Value("${jwt.refreshToken.expireIn}")
    private Integer refreshTokenExpireIn;

    @Value("${jwt.refreshToken.secret}")
    private String refreshTokenSecret;

    /**
     * 获取访问令牌的过期时间（以秒为单位）。
     *
     * @return 访问令牌的过期时间
     */
    public Integer getAccessTokenExpireIn() {
        return accessTokenExpireIn;
    }

    /**
     * 设置访问令牌的过期时间（以秒为单位）。
     *
     * @param accessTokenExpireIn 过期时间值
     */
    public void setAccessTokenExpireIn(Integer accessTokenExpireIn) {
        this.accessTokenExpireIn = accessTokenExpireIn;
    }

    /**
     * 获取访问令牌的密钥。
     *
     * @return 访问令牌的密钥字符串
     */
    public String getAccessTokenSecret() {
        return accessTokenSecret;
    }

    /**
     * 设置访问令牌的密钥。
     *
     * @param accessTokenSecret 密钥字符串
     */
    public void setAccessTokenSecret(String accessTokenSecret) {
        this.accessTokenSecret = accessTokenSecret;
    }

    /**
     * 获取刷新令牌的过期时间（以秒为单位）。
     *
     * @return 刷新令牌的过期时间
     */
    public Integer getRefreshTokenExpireIn() {
        return refreshTokenExpireIn;
    }

    /**
     * 设置刷新令牌的过期时间（以秒为单位）。
     *
     * @param refreshTokenExpireIn 过期时间值
     */
    public void setRefreshTokenExpireIn(Integer refreshTokenExpireIn) {
        this.refreshTokenExpireIn = refreshTokenExpireIn;
    }

    /**
     * 获取刷新令牌的密钥。
     *
     * @return 刷新令牌的密钥字符串
     */
    public String getRefreshTokenSecret() {
        return refreshTokenSecret;
    }

    /**
     * 设置刷新令牌的密钥。
     *
     * @param refreshTokenSecret 密钥字符串
     */
    public void setRefreshTokenSecret(String refreshTokenSecret) {
        this.refreshTokenSecret = refreshTokenSecret;
    }
}
