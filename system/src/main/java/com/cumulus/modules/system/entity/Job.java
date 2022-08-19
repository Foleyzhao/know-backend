package com.cumulus.modules.system.entity;

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
 * 系统岗位实体
 *
 * @author shenjc
 */
@Getter
@Setter
@Entity
@Table(name = "sys_job")
public class Job extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -7854418381595991335L;

    public static final long DEFAULT_DEPT_HEAD_JOB_ID = 2L;

    public static final long DEFAULT_NEW_USER_JOB_ID = 3L;

    /**
     * ID
     */
    @Id
    @NotNull(groups = Update.class)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 岗位名称
     */
    @NotBlank
    private String name;

    /**
     * 岗位排序
     */
    @NotNull
    private Long jobSort;

    /**
     * 是否启用
     */
    @NotNull
    private Boolean enabled;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        Job job = (Job) o;
        return Objects.equals(id, job.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
