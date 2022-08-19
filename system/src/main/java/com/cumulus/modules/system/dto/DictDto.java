package com.cumulus.modules.system.dto;

import com.cumulus.base.BaseDTO;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 系统字典传输对象
 */
@Getter
@Setter
public class DictDto extends BaseDTO implements Serializable {

    private static final long serialVersionUID = 4455662808915726623L;

    /**
     * ID
     */
    private Long id;

    /**
     * 系统字典详情列表
     */
    private List<DictDetailDto> dictDetails;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

}
