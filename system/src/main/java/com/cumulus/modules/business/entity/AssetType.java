package com.cumulus.modules.business.entity;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import com.cumulus.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 资产类型实体
 *
 * @author zhaoff
 */
@Getter
@Setter
@Entity
@Table(name = "tbl_asset_type")
public class AssetType extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 5751772645413694959L;

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

    /**
     * 父资产
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private AssetType parent;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetType assetType = (AssetType) o;
        return Objects.equals(id, assetType.id) && Objects.equals(name, assetType.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
