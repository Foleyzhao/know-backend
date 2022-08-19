package com.cumulus.modules.system.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.cumulus.base.BaseDTO;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * 系统用户传输对象
 *
 * @author shenjc
 */
@Getter
@Setter
public class UserDto extends BaseDTO implements Serializable {

    private static final long serialVersionUID = 4328854092747944797L;

    /**
     * ID
     */
    private Long id;

    /**
     * 系统角色集合
     */
    private Set<SimpRoleDto> roles;

    /**
     * 系统岗位集合
     */
    private JobDto job;

    /**
     * 最小系统部门传输对象
     */
    private SimpDeptDto dept;

    /**
     * 系统部门ID
     */
    private Long deptId;

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

    /**
     * 头像真实名称
     */
    private String avatarName;

    /**
     * 头像存储的路径
     */
    private String avatarPath;

    /**
     * 密码
     */
    @JSONField(serialize = false)
    private String password;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 是否为admin账号
     */
    @JSONField(serialize = false)
    private Boolean isAdmin = false;

    /**
     * 最后修改密码的时间
     */
    private Date pwdResetTime;

    /**
     * 是否是第一次登录  1:是第一次登录 0:不是
     */
    private Integer firstLogin;
}
