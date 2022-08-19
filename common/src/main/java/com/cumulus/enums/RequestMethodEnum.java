package com.cumulus.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 请求类型枚举类
 */
@Getter
@AllArgsConstructor
public enum RequestMethodEnum {

    /**
     * GET请求类型
     */
    GET("GET"),

    /**
     * POST请求类型
     */
    POST("POST"),

    /**
     * PUT请求类型
     */
    PUT("PUT"),

    /**
     * PATCH请求类型
     */
    PATCH("PATCH"),

    /**
     * DELETE请求类型
     */
    DELETE("DELETE"),

    /**
     * 所有请求类型
     */
    ALL("All");

    /**
     * 请求类型
     */
    private final String type;

    /**
     * 根据请求类型获取枚举类请求类型
     *
     * @param type 请求类型
     * @return 枚举类请求方法
     */
    public static RequestMethodEnum find(String type) {
        for (RequestMethodEnum value : RequestMethodEnum.values()) {
            if (type.equals(value.getType())) {
                return value;
            }
        }
        return ALL;
    }

}
