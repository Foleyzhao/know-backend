package com.cumulus.modules.business.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import com.cumulus.base.BaseEntity;

/**
 * 资产系统类型实体
 *
 * @author zhaoff
 */
@Getter
@Setter
@Entity
@Table(name = "tbl_asset_sys_type")
public class AssetSysType extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -5718147310487040472L;

    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 名称
     */
    private String name;

    /**
     * 所属资产类型
     */
    @ManyToOne
    @JoinColumn(name = "asset_type_id")
    private AssetType assetType;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否是自定义资产类型
     */
    private Boolean customize;

    /**
     * 编号
     */
    private String number;

}
