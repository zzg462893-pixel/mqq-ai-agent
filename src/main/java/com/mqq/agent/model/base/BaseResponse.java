package com.mqq.agent.model.base;

import lombok.Data;

import java.io.Serializable;

@Data
public class BaseResponse<T> implements Serializable {

    private int code;

    private T data;

    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(ResultCode resultCode, T data) {
        this(resultCode.getCode(), data, resultCode.getMessage());
    }

    public BaseResponse(ResultCode resultCode) {
        this(resultCode.getCode(), null, resultCode.getMessage());
    }

    public static <T> BaseResponse<T> success() {
        return new BaseResponse<T>(ResultCode.SUCCESS);
    }

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<T>(ResultCode.SUCCESS, data);
    }

    public static <T> BaseResponse<T> error(ResultCode resultCode, String message) {
        return new BaseResponse<T>(resultCode.getCode(), null, message);
    }

    public static <T> BaseResponse<T> error(int resultCode, String message) {
        return new BaseResponse<T>(resultCode, null, message);
    }

    public static <T> BaseResponse<T> error(String message) {
        return new BaseResponse<T>(ResultCode.SYSTEM_ERROR.getCode(), null, message);
    }
}