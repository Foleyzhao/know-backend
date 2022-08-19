package com.cumulus.modules.security.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * 认证用户传输对象
 */
@Getter
@Setter
public class AuthUserDto {

    /**
     * 用户名
     */
    @NotBlank
    private String username;

    /**
     * 密码
     */
    @NotBlank
    private String password;

    /**
     * 验证码
     */
    private String code;

    /**
     * uuid
     */
    private String uuid = "";
}
