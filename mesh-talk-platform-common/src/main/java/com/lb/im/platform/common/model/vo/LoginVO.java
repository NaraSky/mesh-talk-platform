package com.lb.im.platform.common.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("用户登录VO")
public class LoginVO {

    @ApiModelProperty(value = "每次请求都必须在header中携带accessToken")
    private String accessToken;

    @ApiModelProperty(value = "accessToken过期时间(秒)")
    private Integer accessTokenExpiresIn;

    @ApiModelProperty(value = "accessToken过期后，通过refreshToken换取新的token")
    private String refreshToken;

    @ApiModelProperty(value = "refreshToken过期时间(秒)")
    private Integer refreshTokenExpiresIn;

    public LoginVO() {
    }

    public LoginVO(String accessToken, Integer accessTokenExpiresIn, String refreshToken, Integer refreshTokenExpiresIn) {
        this.accessToken = accessToken;
        this.accessTokenExpiresIn = accessTokenExpiresIn;
        this.refreshToken = refreshToken;
        this.refreshTokenExpiresIn = refreshTokenExpiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Integer getAccessTokenExpiresIn() {
        return accessTokenExpiresIn;
    }

    public void setAccessTokenExpiresIn(Integer accessTokenExpiresIn) {
        this.accessTokenExpiresIn = accessTokenExpiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Integer getRefreshTokenExpiresIn() {
        return refreshTokenExpiresIn;
    }

    public void setRefreshTokenExpiresIn(Integer refreshTokenExpiresIn) {
        this.refreshTokenExpiresIn = refreshTokenExpiresIn;
    }
}
