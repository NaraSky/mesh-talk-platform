package com.lb.im.platform.common.response;

import com.lb.im.platform.common.model.enums.HttpCode;

/**
 * 响应消息工厂类，提供创建成功和错误响应消息的静态方法。
 */
public class ResponseMessageFactory {

    /**
     * 创建一个成功响应消息，不包含数据，使用默认的成功状态码和消息。
     */
    public static <T> ResponseMessage<T> getSuccessResponseMessage() {
        ResponseMessage<T> responseMessage = new ResponseMessage<>();
        responseMessage.setCode(HttpCode.SUCCESS.getCode());
        responseMessage.setMessage(HttpCode.SUCCESS.getMsg());
        return responseMessage;
    }

    /**
     * 创建一个包含指定数据的成功响应消息，使用默认的成功状态码和消息。
     */
    public static <T> ResponseMessage<T> getSuccessResponseMessage(T data) {
        ResponseMessage<T> responseMessage = new ResponseMessage<>();
        responseMessage.setCode(HttpCode.SUCCESS.getCode());
        responseMessage.setMessage(HttpCode.SUCCESS.getMsg());
        responseMessage.setData(data);
        return responseMessage;
    }

    /**
     * 创建一个包含指定数据和自定义消息的成功响应消息，使用默认的成功状态码。
     */
    public static <T> ResponseMessage<T> getSuccessResponseMessage(T data, String message) {
        ResponseMessage<T> responseMessage = new ResponseMessage<>();
        responseMessage.setCode(HttpCode.SUCCESS.getCode());
        responseMessage.setMessage(message);
        responseMessage.setData(data);
        return responseMessage;
    }

    /**
     * 创建一个不包含数据但带有自定义消息的成功响应消息，使用默认的成功状态码。
     */
    public static <T> ResponseMessage<T> getSuccessResponseMessage(String message) {
        ResponseMessage<T> responseMessage = new ResponseMessage<>();
        responseMessage.setCode(HttpCode.SUCCESS.getCode());
        responseMessage.setMessage(message);
        return responseMessage;
    }

    /**
     * 创建一个带有指定错误码和消息的错误响应消息。
     */
    public static <T> ResponseMessage<T> getErrorResponseMessage(Integer code, String message) {
        ResponseMessage<T> responseMessage = new ResponseMessage<>();
        responseMessage.setCode(code);
        responseMessage.setMessage(message);
        return responseMessage;
    }

    /**
     * 创建一个基于指定HttpCode枚举的错误响应消息，允许自定义错误消息。
     */
    public static <T> ResponseMessage<T> getErrorResponseMessage(HttpCode httpCode, String message) {
        ResponseMessage<T> responseMessage = new ResponseMessage<>();
        responseMessage.setCode(httpCode.getCode());
        responseMessage.setMessage(message);
        return responseMessage;
    }

    /**
     * 创建一个基于指定HttpCode枚举的错误响应消息，使用枚举中的默认消息。
     */
    public static <T> ResponseMessage<T> getErrorResponseMessage(HttpCode httpCode) {
        ResponseMessage<T> responseMessage = new ResponseMessage<>();
        responseMessage.setCode(httpCode.getCode());
        responseMessage.setMessage(httpCode.getMsg());
        return responseMessage;
    }
}
