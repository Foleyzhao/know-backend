package com.cumulus.modules.system.dto;

import com.cumulus.base.BaseDTO;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 系统菜单传输对象
 *
 * @author shenjc
 */
@Getter
@Setter
public class MenuDto extends BaseDTO implements Serializable {

    private static final long serialVersionUID = 4402374339003181041L;

    /**
     * ID
     */
    private Long id;

    /**
     * 子菜单传输对象列表
     */
    private List<MenuDto> children;

    /**
     * 菜单类型
     */
    private Integer type;

    /**
     * 权限标识
     */
    private String permission;

    /**
     * 菜单标题
     */
    private String title;

    /**
     * 排序
     */
    private Integer menuSort;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 父菜单ID
     */
    private Long pid;

    /**
     * 子菜单数量
     */
    private Integer subCount;

    /**
     * 外链菜单
     */
    private Boolean iFrame;

    /**
     * 缓存
     */
    private Boolean cache;

    /**
     * 是否隐藏
     */
    private Boolean hidden;

    /**
     * 菜单组件名称
     */
    private String componentName;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 判断是否有子菜单
     *
     * @return 是否有子菜单
     */
    public Boolean getHasChildren() {
        return subCount > 0;
    }

    /**
     * 判断是否是最底层菜单
     *
     * @return 是否是最底层菜单
     */
    public Boolean getLeaf() {
        return subCount <= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        MenuDto menuDto = (MenuDto) o;
        return Objects.equals(id, menuDto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
