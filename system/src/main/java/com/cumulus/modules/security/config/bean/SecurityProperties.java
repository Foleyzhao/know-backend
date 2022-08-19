package com.cumulus.modules.security.config.bean;

import lombok.Data;

/**
 * Jwt参数配置
 */
@Data
public class SecurityProperties {

    /**
     * token对的请求头的key（默认：Authorization）
     */
    private String header;

    /**
     * token前缀（默认Bearer）
     */
    private String tokenStartWith;

    /**
     * token签名key
     */
    private String base64Secret;

    /**
     * token过期时间（单位：毫秒）
     */
    private Long tokenValidityInSeconds;

    /**
     * 在线用户redis key前缀
     */
    private String onlineKey;

    /**
     * 验证码redis key前缀
     */
    private String codeKey;

    /**
     * token续期检查时间
     */
    private Long detect;

    /**
     * 续期时间
     */
    private Long renew;

    /**
     * 获取token前缀
     *
     * @return token前缀
     */
    public String getTokenStartWith() {
        return tokenStartWith + " ";
    }

}
