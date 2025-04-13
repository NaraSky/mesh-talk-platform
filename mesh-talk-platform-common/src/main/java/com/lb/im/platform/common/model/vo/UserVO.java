package com.lb.im.platform.common.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@ApiModel("用户信息VO")
public class UserVO {

    @NotNull(message = "用户id不能为空")
    @ApiModelProperty(value = "id")
    private Long id;

    @NotEmpty(message = "用户名不能为空")
    @Length(max = 64, message = "用户名不能大于64字符")
    @ApiModelProperty(value = "用户名")
    private String userName;

    @NotEmpty(message = "用户昵称不能为空")
    @Length(max = 64, message = "昵称不能大于64字符")
    @ApiModelProperty(value = "用户昵称")
    private String nickName;

    @ApiModelProperty(value = "性别")
    private Integer sex;

    @ApiModelProperty(value = "用户类型 1:普通用户 2:审核账户")
    private Integer type;

    @Length(max = 1024, message = "个性签名不能大于1024个字符")
    @ApiModelProperty(value = "个性签名")
    private String signature;

    @ApiModelProperty(value = "头像")
    private String headImage;

    @ApiModelProperty(value = "头像缩略图")
    private String headImageThumb;

    @ApiModelProperty(value = "是否在线")
    private Boolean online;


    public UserVO() {
    }

    public UserVO(Long id, String userName, String nickName, Integer sex, Integer type, String signature, String headImage, String headImageThumb, Boolean online) {
        this.id = id;
        this.userName = userName;
        this.nickName = nickName;
        this.sex = sex;
        this.type = type;
        this.signature = signature;
        this.headImage = headImage;
        this.headImageThumb = headImageThumb;
        this.online = online;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getHeadImage() {
        return headImage;
    }

    public void setHeadImage(String headImage) {
        this.headImage = headImage;
    }

    public String getHeadImageThumb() {
        return headImageThumb;
    }

    public void setHeadImageThumb(String headImageThumb) {
        this.headImageThumb = headImageThumb;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }
}
