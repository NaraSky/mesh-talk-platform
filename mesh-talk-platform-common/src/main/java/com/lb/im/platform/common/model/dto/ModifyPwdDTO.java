package com.lb.im.platform.common.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;

@ApiModel("修改密码DTO")
public class ModifyPwdDTO {

    @NotEmpty(message = "旧用户密码不可为空")
    @ApiModelProperty(value = "旧用户密码")
    private String oldPassword;

    @NotEmpty(message = "新用户密码不可为空")
    @ApiModelProperty(value = "新用户密码")
    private String newPassword;

    public ModifyPwdDTO() {
    }

    public ModifyPwdDTO(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}