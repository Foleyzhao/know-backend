package com.cumulus.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 验证码业务场景枚举类
 */
@Getter
@AllArgsConstructor
public enum CodeEnum {

    /**
     * 通过手机号码重置邮箱
     */
    PHONE_RESET_EMAIL_CODE("phone_reset_email_code_", "通过手机号码重置邮箱"),

    /**
     * 通过旧邮箱重置邮箱
     */
    EMAIL_RESET_EMAIL_CODE("email_reset_email_code_", "通过旧邮箱重置邮箱"),

    /**
     * 通过手机号码重置密码
     */
    PHONE_RESET_PWD_CODE("phone_reset_pwd_code_", "通过手机号码重置密码"),

    /**
     * 通过邮箱重置密码
     */
    EMAIL_RESET_PWD_CODE("email_reset_pwd_code_", "通过邮箱重置密码");

    /**
     * 场景对应验证码在Redis中的key
     */
    private final String key;

    /**
     * 场景描述
     */
    private final String description;

}
