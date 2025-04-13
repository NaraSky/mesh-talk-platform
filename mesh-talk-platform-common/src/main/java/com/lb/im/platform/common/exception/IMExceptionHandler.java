package com.lb.im.platform.common.exception;

import com.lb.im.platform.common.model.enums.HttpCode;
import com.lb.im.platform.common.response.ResponseMessage;
import com.lb.im.platform.common.response.ResponseMessageFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局异常处理类，使用Spring的@ControllerAdvice注解实现全局异常捕获与响应处理
 */
@ControllerAdvice
public class IMExceptionHandler {

    /**
     * 处理IMException异常，返回包含具体错误码和错误信息的响应对象
     */
    @ResponseBody
    @ExceptionHandler(IMException.class)
    public ResponseMessage<String> handleIMException(IMException e) {
        return ResponseMessageFactory.getErrorResponseMessage(e.getCode(), e.getMessage());
    }

    /**
     * 处理未被捕获的通用异常，返回系统内部错误的预设响应
     */
    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ResponseMessage<String> handleException(Exception e) {
        return ResponseMessageFactory.getErrorResponseMessage(HttpCode.PROGRAM_ERROR);
    }
}
