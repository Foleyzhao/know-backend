package com.cumulus.modules.system.dto;

import com.cumulus.base.BaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 系统岗位传输对象
 */
@Getter
@Setter
@NoArgsConstructor
public class JobDto extends BaseDTO implements Serializable {

    private static final long serialVersionUID = 5529560217961636883L;

    /**
     * ID
     */
    private Long id;

    /**
     * 岗位排序
     */
    private Integer jobSort;

    /**
     * 岗位名称
     */
    private String name;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 岗位名称
     *
     * @param name    岗位名称
     * @param enabled 是否启用
     */
    public JobDto(String name, Boolean enabled) {
        this.name = name;
        this.enabled = enabled;
    }

}