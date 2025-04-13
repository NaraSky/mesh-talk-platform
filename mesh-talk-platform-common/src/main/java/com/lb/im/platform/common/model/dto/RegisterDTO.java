package com.lb.im.platform.common.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@ApiModel("用户注册DTO")
public class RegisterDTO {

    @Length(max = 64, message = "用户名不能大于64字符")
    @NotEmpty(message = "用户名不可为空")
    @ApiModelProperty(value = "用户名")
    private String userName;

    @Length(min = 5, max = 20, message = "密码长度必须在5-20个字符之间")
    @NotEmpty(message = "用户密码不可为空")
    @ApiModelProperty(value = "用户密码")
    private String password;

    @Length(max = 64, message = "昵称不能大于64字符")
    @NotEmpty(message = "用户昵称不可为空")
    @ApiModelProperty(value = "用户昵称")
    private String nickName;

    public RegisterDTO() {
    }

    public RegisterDTO(String userName, String password, String nickName) {
        this.userName = userName;
        this.password = password;
        this.nickName = nickName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}
