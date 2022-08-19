package com.cumulus.modules.system.entity;

import com.cumulus.annotation.SizeChinese;
import com.cumulus.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * 系统部门实体
 *
 * @author shenjc
 */
@Getter
@Setter
@Entity
@Table(name = "sys_dept")
public class Dept extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 8685580515729573253L;

    /**
     * 启用 为TRUE
     */
    public static final boolean ENABLE = true;

    /**
     * 默认部门排序 999
     */
    public static final int DEFAULT_DEPT_SORT = 999;

    /**
     * 部门名最大长度
     */
    public static final int MAX_NAME_SIZE = 20;

    /**
     * ID
     */
    @NotNull(groups = Update.class)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 排序
     */
    private Integer deptSort;

    /**
     * 部门名称
     */
    @NotBlank(groups = Update.class, message = "部门名称不能为空")
    @SizeChinese(max = MAX_NAME_SIZE, min = 0, groups = Update.class, message = "部门名长度不正确")
    private String name;

    /**
     * 是否启用
     */
    @NotNull
    private Boolean enabled;

    /**
     * 上级部门ID
     */
    private Long pid;

    /**
     * 子部门数量
     */
    private Integer subCount = 0;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        Dept dept = (Dept) o;
        return Objects.equals(id, dept.id) && Objects.equals(name, dept.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

}
