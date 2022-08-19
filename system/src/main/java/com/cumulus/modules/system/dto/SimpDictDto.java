package com.cumulus.modules.system.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 精简的系统字典传输对象
 */
@Getter
@Setter
public class SimpDictDto implements Serializable {

    private static final long serialVersionUID = -3171248309055547491L;

    /**
     * ID
     */
    private Long id;

}
