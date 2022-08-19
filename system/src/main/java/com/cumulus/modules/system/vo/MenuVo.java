package com.cumulus.modules.system.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 菜单Vo类（构建前端路由时用到）
 *
 * @author shenjc
 */
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MenuVo implements Serializable {

    private static final long serialVersionUID = 6074408233979819874L;

    public static final String DEFAULT_COMPONENT_LAYOUT = "Layout";

    /**
     * 菜单组件名称
     */
    private String name;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 是否隐藏
     */
    private Boolean hidden;

    /**
     * 是否重定向
     */
    private String redirect;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 是否总是显示
     */
    private Boolean alwaysShow;

    /**
     * 元菜单
     */
    private MenuMetaVo meta;

    /**
     * 子菜单列表
     */
    private List<MenuVo> children;

}
