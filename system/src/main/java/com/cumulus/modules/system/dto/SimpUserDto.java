package com.cumulus.modules.system.dto;

import com.cumulus.base.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author : shenjc
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SimpUserDto extends BaseDTO implements Serializable {
    private static final long serialVersionUID = -1422563955088528945L;

    /**
     * ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 性别
     */
    private String gender;
}
