package com.mqq.agent.model.base;

import lombok.Getter;

@Getter
public enum ResultCode {

    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    FILE_PARES_ERROR(40001, "文件解析异常"),
    BUSINESS_ERROR(40002, "通用业务异常"),
    MYSQL_ERROR(40003, "MySQL数据库操作异常"),
    REDIS_ERROR(40004, "Redis操作异常"),
    SYSTEM_ERROR(50000, "系统内部异常");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}