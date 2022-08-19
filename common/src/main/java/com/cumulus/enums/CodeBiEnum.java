package com.cumulus.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 验证码业务场景枚举类
 */
@Getter
@AllArgsConstructor
public enum CodeBiEnum {

    /**
     * 修改邮箱
     */
    ONE(1, "修改邮箱"),

    /**
     * 通过邮箱修改密码
     */
    TWO(2, "通过邮箱修改密码");

    /**
     * 场景编码
     */
    private final Integer code;

    /**
     * 场景描述
     */
    private final String description;

    /**
     * 通过场景编码返回枚举类型场景
     *
     * @param code 场景编码
     * @return 枚举类型场景
     */
    public static CodeBiEnum find(Integer code) {
        for (CodeBiEnum value : CodeBiEnum.values()) {
            if (code.equals(value.getCode())) {
                return value;
            }
        }
        return null;
    }

}
