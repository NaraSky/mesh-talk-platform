package com.lb.im.platform.common.model.enums;

public enum HttpCode {

    SUCCESS(200,"成功"),
    NO_LOGIN(400,"未登录"),
    INVALID_TOKEN(401,"token无效或已过期"),
    PARAMS_ERROR(402,"参数错误"),
    PROGRAM_ERROR(500,"系统繁忙，请稍后再试"),
    PASSWORD_ERROR(10001, "密码不正确"),
    USERNAME_ALREADY_REGISTER(10003,"该用户名已注册"),
    XSS_PARAM_ERROR(10004,"请不要输入非法内容");

    private Integer code;
    private String msg;

    HttpCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
