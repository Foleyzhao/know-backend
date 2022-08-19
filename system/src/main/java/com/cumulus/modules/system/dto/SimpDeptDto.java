package com.cumulus.modules.system.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 精简的系统部门传输对象
 */
@Data
public class SimpDeptDto implements Serializable {

    private static final long serialVersionUID = 6679074862908842237L;

    /**
     * ID
     */
    private Long id;

    /**
     * 部门名称
     */
    private String name;

}
