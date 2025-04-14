package com.lb.im.platform.common.risk.enums;

public enum RuleEnum {

    XSS(0, "XSS安全服务"),
    IP(1, "IP安全服务"),
    PATH(2, "资源安全服务"),
    AUTH(10, "账号安全服务");

    private final Integer code;
    private final String message;

    RuleEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
