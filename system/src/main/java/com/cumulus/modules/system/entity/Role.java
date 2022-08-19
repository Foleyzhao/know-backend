package com.cumulus.modules.system.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.cumulus.annotation.SizeChinese;
import com.cumulus.base.BaseEntity;
import com.cumulus.enums.DataScopeEnum;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

/**
 * 系统角色实体
 */
@Getter
@Setter
@Entity
@Table(name = "sys_role")
public class Role extends BaseEntity implements Serializable {


    private static final long serialVersionUID = -7611802378944108149L;

    public static final int DEFAULT_LEVEL = 3;

    /**
     * ID
     */
    @Id
    @Null(groups = Create.class, message = "新增权限组不需要id")
    @NotNull(groups = {Update.class}, message = "修改权限组需要id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户
     */
    @ManyToMany
    @JoinTable(name = "sys_users_roles", joinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")})
    @JSONField(serialize = false)
    private Set<User> users;

    /**
     * 菜单
     */
    @ManyToMany
    @JoinTable(name = "sys_roles_menus", joinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "menu_id", referencedColumnName = "id")})
    private Set<Menu> menus;

    /**
     * 名称
     */
    @NotBlank(groups = {Create.class}, message = "权限组名称不能为空")
    @SizeChinese(min = 0, max = 30, groups = {Create.class, Update.class}, message = "权限组名长度不正确")
    private String name;

    /**
     * 数据权限（全部、本级、自定义）
     */
    private String dataScope = DataScopeEnum.ALL.getValue();

    /**
     * 级别（数值越小，级别越大）
     */
    @Column(name = "level")
    private Integer level = 3;

    /**
     * 描述
     */
    @SizeChinese(min = 0, max = 200, groups = {Create.class, Update.class}, message = "权限组描述过长")
    private String description;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        Role role = (Role) o;
        return Objects.equals(id, role.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
