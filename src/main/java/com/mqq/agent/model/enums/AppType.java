package com.mqq.agent.model.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * 应用类型
 */
@Getter
public enum AppType {

    LOVE_APP("love_app", "恋爱大师应用");

    /**
     * 应用类型编码
     */
    private final String code;

    /**
     * 描述
     */
    private final String description;

    AppType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static boolean checkCode(String code) {
        return Arrays.stream(values()).anyMatch(e -> e.getCode().equals(code));
    }
}