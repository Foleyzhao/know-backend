package com.cumulus.modules.system.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单树结构Dto
 *
 * @author : shenjc
 */
@Getter
@Setter
public class MenuTreeDto implements Serializable {
    private static final long serialVersionUID = 8052150494188675883L;


    /**
     * ID
     */
    private Long id;

    /**
     * 子菜单传输对象列表
     */
    private List<MenuTreeDto> children = new ArrayList<>();

    /**
     * 菜单标题
     */
    private String title;

    /**
     * 排序
     */
    private Integer menuSort;

    /**
     * 父菜单ID
     */
    private Long pid;

    /**
     * 子菜单数量
     */
    private Integer subCount;
}
