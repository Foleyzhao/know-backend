package com.cumulus.modules.security.config.bean;

import lombok.Data;

/**
 * 登录验证码配置信息
 *
 * @author shenjc
 */
@Data
public class LoginCode {

    /**
     * 验证码配置
     */
    private LoginCodeEnum codeType;

    /**
     * 验证码有效期（单位：分钟）
     */
    private Long expiration = 2L;

    /**
     * 验证码内容长度
     */
    private int length = 2;

    /**
     * 验证码宽度
     */
    private int width = 180;

    /**
     * 验证码高度
     */
    private int height = 50;

    /**
     * 验证码字体
     */
    private String fontName;

    /**
     * 字体大小
     */
    private int fontSize = 25;

    /**
     * 验证码字符类型
     */
    private CaptchaCharType charType = CaptchaCharType.TYPE_DEFAULT;

    public LoginCodeEnum getCodeType() {
        return codeType;
    }

}
