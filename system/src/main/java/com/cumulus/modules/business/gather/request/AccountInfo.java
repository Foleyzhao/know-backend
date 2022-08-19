package com.cumulus.modules.business.gather.request;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 资产账号信息
 *
 * @author zhaoff
 */
@Getter
@Setter
public class AccountInfo implements Serializable {

    private static final long serialVersionUID = 4124689396225310888L;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

}
