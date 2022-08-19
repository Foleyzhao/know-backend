package com.cumulus.modules.system.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 元菜单Vo类
 */
@Data
@AllArgsConstructor
public class MenuMetaVo implements Serializable {

    private static final long serialVersionUID = 2290822569142727494L;

    /**
     * 菜单标题
     */
    private String title;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 是否不缓存
     */
    private Boolean noCache;

}
