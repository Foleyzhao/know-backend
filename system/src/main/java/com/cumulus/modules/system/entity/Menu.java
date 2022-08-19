package com.cumulus.modules.system.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.cumulus.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

/**
 * 系统菜单实体 permission 权限值为空的权限意为默认权限所有角色享有 其他存在 permission 的权限均需要赋予后才可以使用
 *
 * @author shenjc
 */
@Getter
@Setter
@Entity
@Table(name = "sys_menu")
public class Menu extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 8129354600986104711L;

    /**
     * 默认的权限id
     */
    public static final long DEFAULT_VUL_SCAN_MENU_ID = 12L;
    public static final long DEFAULT_DETECT_MENU_ID = 18L;
    public static final long DEFAULT_RISK_DISPOSAL_MENU_ID = 15L;
    public static final long DEFAULT_GATHER_MENU_ID = 20L;

    /**
     * ID
     */
    @Id
    @NotNull(groups = {Update.class})
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 菜单角色
     */
    @JSONField(serialize = false)
    @ManyToMany(mappedBy = "menus")
    private Set<Role> roles;

    /**
     * 菜单标题
     */
    private String title;

    /**
     * 菜单组件名称
     */
    @Column(name = "component_name")
    private String componentName;

    /**
     * 排序
     */
    private Integer menuSort = 999;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 菜单类型（目录、菜单、按钮）
     */
    private Integer type;

    /**
     * 权限标识
     */
    private String permission;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 缓存
     */
    @Column(columnDefinition = "bit(1) default 0")
    private Boolean cache;

    /**
     * 是否隐藏
     */
    @Column(columnDefinition = "bit(1) default 0")
    private Boolean hidden;

    /**
     * 上级菜单
     */
    private Long pid;

    /**
     * 子节点数目
     */
    private Integer subCount = 0;

    /**
     * 外链菜单
     */
    private Boolean iFrame;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        Menu menu = (Menu) o;
        return Objects.equals(id, menu.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
