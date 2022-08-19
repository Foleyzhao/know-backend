package com.cumulus.modules.system.vo;

import lombok.Data;

/**
 * 修改密码的VO类
 *
 * @author shenjc
 */
@Data
public class UserPassVo {

    /**
     * 旧密码
     */
    private String oldPass;

    /**
     * 新密码
     */
    private String newPass;

}
