package com.cumulus.modules.system.dto;

import com.cumulus.base.BaseDTO;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

/**
 * 系统角色传输对象
 *
 * @author shenjc
 */
@Getter
@Setter
public class RoleDto extends BaseDTO implements Serializable {

    private static final long serialVersionUID = -6451378309654828637L;

    /**
     * ID
     */
    private Long id;

    /**
     * 用户
     */
    private Set<SimpUserDto> users;

    /**
     * 名称
     */
    private String name;

    /**
     * 数据权限
     */
    private String dataScope;

    /**
     * 级别
     */
    private Integer level;

    /**
     * 描述
     */
    private String description;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        RoleDto roleDto = (RoleDto) o;
        return Objects.equals(id, roleDto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
