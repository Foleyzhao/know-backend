package com.cumulus.modules.system.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 精简的系统岗位传输对象
 */
@Data
@NoArgsConstructor
public class SimpJobDto implements Serializable {

    private static final long serialVersionUID = -1000427306872525768L;

    /**
     * ID
     */
    private Long id;

    /**
     * 岗位名称
     */
    private String name;

}
