package com.lb.im.platform.common.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@ApiModel("群信息VO")
public class GroupVO {

    @ApiModelProperty(value = "群id")
    private Long id;

    @Length(max = 20, message = "群名称长度不能大于20")
    @NotEmpty(message = "群名称不可为空")
    @ApiModelProperty(value = "群名称")
    private String name;

    @ApiModelProperty(value = "群主id")
    private Long ownerId;

    @ApiModelProperty(value = "头像")
    private String headImage;

    @ApiModelProperty(value = "头像缩略图")
    private String headImageThumb;

    @Length(max = 1024, message = "群聊显示长度不能大于1024")
    @ApiModelProperty(value = "群公告")
    private String notice;

    @Length(max = 20, message = "群聊显示长度不能大于20")
    @ApiModelProperty(value = "用户在群显示昵称")
    private String aliasName;

    @Length(max = 20, message = "群聊显示长度不能大于20")
    @ApiModelProperty(value = "群聊显示备注")
    private String remark;

    public GroupVO() {
    }

    public GroupVO(Long id, String name, Long ownerId, String headImage, String headImageThumb, String notice, String aliasName, String remark) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.headImage = headImage;
        this.headImageThumb = headImageThumb;
        this.notice = notice;
        this.aliasName = aliasName;
        this.remark = remark;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
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

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
