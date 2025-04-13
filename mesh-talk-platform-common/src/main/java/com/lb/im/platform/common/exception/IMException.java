package com.lb.im.platform.common.exception;

import com.lb.im.platform.common.model.enums.HttpCode;

public class IMException extends RuntimeException {

    private static final long serialVersionUID = 2849816842118016526L;

    private Integer code;
    private String message;

    public IMException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public IMException(HttpCode httpCode, String message) {
        this.code = httpCode.getCode();
        this.message = message;
    }

    public IMException(HttpCode httpCode){
        this.code = httpCode.getCode();
        this.message = httpCode.getMsg();
    }

    public IMException(String message) {
        this.code = HttpCode.PROGRAM_ERROR.getCode();
    }

    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
