package com.cumulus.modules.system.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 精简系统角色传输对象
 */
@Data
public class SimpRoleDto implements Serializable {

    private static final long serialVersionUID = -586215111170757416L;

    /**
     * ID
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 级别
     */
    private Integer level;

    /**
     * 数据权限
     */
    private String dataScope;

}
