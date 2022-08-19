package com.cumulus.modules.system.dto;

import com.cumulus.base.BaseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 系统部门传输对象
 *
 * @author shenjc
 */
@Getter
@Setter
public class DeptDto extends BaseDTO implements Serializable {

    private static final long serialVersionUID = -2531492969078061135L;

    /**
     * ID
     */
    private Long id;

    /**
     * 部门名称
     */
    private String name;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 排序
     */
    private Integer deptSort;

    /**
     * 子部门列表
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<DeptDto> children;

    /**
     * 上级部门ID
     */
    private Long pid;

    /**
     * 子部门数量
     */
    private Integer subCount;

    /**
     * 部门负责人
     */
    private UserDto deptHead;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        DeptDto deptDto = (DeptDto) o;
        return Objects.equals(id, deptDto.id) && Objects.equals(name, deptDto.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

}