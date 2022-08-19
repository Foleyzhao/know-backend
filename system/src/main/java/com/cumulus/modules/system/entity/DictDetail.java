package com.cumulus.modules.system.entity;

import com.cumulus.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 系统字典详情实体
 */
@Getter
@Setter
@Entity
@Table(name = "sys_dict_detail")
public class DictDetail extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 8635665934636483785L;

    /**
     * ID
     */
    @Id
    @NotNull(groups = Update.class)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 字典
     */
    @JoinColumn(name = "dict_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Dict dict;

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
    private Integer dictSort = 999;

}
