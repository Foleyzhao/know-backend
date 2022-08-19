package com.cumulus.modules.system.dto;

import com.cumulus.base.BaseDTO;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 系统字典详情传输对象
 */
@Getter
@Setter
public class DictDetailDto extends BaseDTO implements Serializable {

    private static final long serialVersionUID = 1886759359765788493L;

    /**
     * ID
     */
    private Long id;

    /**
     * 最小系统字典传输对象
     */
    private SimpDictDto dict;

    /**
     * 字典详情标签
     */
    private String label;

    /**
     * 字典详情值
     */
    private String value;

    /**
     * 排序
     */
    private Integer dictSort;

}
