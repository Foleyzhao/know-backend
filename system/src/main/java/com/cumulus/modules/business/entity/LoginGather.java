package com.cumulus.modules.business.entity;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zhangxq
 */
@Getter
@Setter
public class LoginGather implements Serializable {

    private static final long serialVersionUID = -6853300287363836204L;

    /**
     * 账号
     */
    private String account;

    /**
     * 密码
     */
    private String pwd;

    /**
     * 协议
     */
    private String protocol;

    /**
     * 端口
     */
    private Integer port;
}
