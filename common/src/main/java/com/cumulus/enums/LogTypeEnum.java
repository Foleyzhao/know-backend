package com.cumulus.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 日志类型枚举类
 *
 * @author : shenjc
 */
@Getter
@AllArgsConstructor
public enum LogTypeEnum {

    /**
     * 日志枚举类 value 是存在数据库中的
     */
    INFO("INFO", "默认日志"),
    ERROR("ERROR", "报错日志"),
    LOGIN("0", "登录日志");

    private final String value;

    private final String description;

    public static String valueToDescription(String value) {
        for (LogTypeEnum logTypeEnum : LogTypeEnum.values()) {
            if (logTypeEnum.getValue().equals(value)) {
                return logTypeEnum.getDescription();
            }
        }
        return LogTypeEnum.INFO.getDescription();
    }
}
