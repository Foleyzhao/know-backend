package com.cumulus.modules.system.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.cumulus.annotation.SizeChinese;
import com.cumulus.base.BaseEntity;
import com.cumulus.utils.RegexUtil;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

/**
 * 系统用户实体
 *
 * @author shenjc
 */
@Getter
@Setter
@Entity
@Table(name = "sys_user")
public class User extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -3717548215123813457L;

    /**
     * 新建用户的初始密码
     */
    public static final String DEFAULT_PWD = "test@2020!";

    /**
     * 一些默认值
     */
    public static final boolean ENABLE = true;
    public static final String DEFAULT_GENDER = "未知";
    public static final long DEFAULT_SUPER_MANAGER_ID = 1L;
    public static final long DEFAULT_RISK_MANAGER_ID = 3L;
    public static final String DEFAULT_SUPER_MANAGER_NAME = "system";

    /**
     * 是否是第一次登录 1:是第一次登录 0:不是
     */
    public static final int FIRST_LOGIN = 1;
    public static final int NOT_FIRST_LOGIN = 0;

    /**
     * ID
     */
    @Id
    @NotNull(groups = Update.class, message = "更新用户需要id")
    @Null(groups = {Create.class, CreateDept.class}, message = "新增用户不能拥有id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户角色
     */
    @ManyToMany(mappedBy = "users", fetch = FetchType.EAGER)
    @NotEmpty(groups = Create.class, message = "权限组不能为空")
    private Set<Role> roles;

    /**
     * 用户岗位
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "job_id")
    private Job job;

    /**
     * 用户部门
     */
    @OneToOne
    @JoinColumn(name = "dept_id")
    @NotNull(groups = {Create.class, CreateDept.class}, message = "部门不能为空")
    private Dept dept;

    /**
     * 用户名
     */
    @NotBlank(groups = {Create.class, CreateDept.class}, message = "用户名不能为空")
    @Size(max = 30, min = 0, groups = {Create.class, CreateDept.class}, message = "用户名长度不正确")
    @Pattern(regexp = RegexUtil.NUMBER_CHARACTER,
            groups = {Create.class, CreateDept.class}, message = "用户名只能使用数字和英文")
    @Column(unique = true)
    private String username;

    /**
     * 用户昵称
     */
    @NotBlank(groups = {Create.class, CreateDept.class}, message = "姓名不能为空")
    @SizeChinese(max = 30, min = 0, groups = {Create.class, CreateDept.class}, message = "姓名长度不正确")
    private String nickName;

    /**
     * 邮箱
     */
    @Email(groups = {Create.class, Update.class}, message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号码
     */
    @NotBlank(groups = {Create.class}, message = "手机号码为空")
    private String phone;

    /**
     * 用户性别
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
    private Boolean isAdmin = false;

    /**
     * 是否是第一次登录
     */
    private Integer firstLogin;

    /**
     * 最后修改密码的时间
     */
    @Column(name = "pwd_reset_time")
    private Date pwdResetTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return Objects.equals(id, user.id) &&
                Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }

    /**
     * 创建部门实体时的用户校验
     */
    public @interface CreateDept {
    }
}
