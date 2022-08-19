package com.cumulus.modules.system.entity;

import com.cumulus.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 系统字典实体
 */
@Getter
@Setter
@Entity
@Table(name = "sys_dict")
public class Dict extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -6389841122757276548L;

    /**
     * ID
     */
    @Id
    @NotNull(groups = Update.class)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 系统字典详情列表
     */
    @OneToMany(mappedBy = "dict", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private List<DictDetail> dictDetails;

    /**
     * 名称
     */
    @NotBlank
    private String name;

    /**
     * 名称
     */
    private String description;

}
