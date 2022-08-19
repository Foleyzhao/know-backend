package com.cumulus.modules.business.entity;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import com.cumulus.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

/**
 * 资产标签实体类
 *
 * @author zhangxq
 */
@Setter
@Getter
@Entity
@Table(name = "tbl_asset_tag")
@DynamicUpdate
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AssetTag extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 7165875722496245317L;

    /**
     * 数据库
     */
    public static final long ASSET_TAG_DB = 3;

    /**
     * 中间件
     */
    public static final long ASSET_TAG_MIDDLEWARE = 2;

    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 类型名称
     */
    @NotBlank
    @Column(name = "`name`")
    private String name;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 是否是内置标签 false 自定义 true 定制 默认
     */
    private Boolean customize;

    /**
     * 备注
     */
    @Column(name = "`description`")
    private String description;

    /**
     * 父资产
     */
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "parent_id")
    private AssetTag parent;

    /**
     * 编号
     */
    private String number;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        AssetTag assetTag = (AssetTag) o;
        return Objects.equals(id, assetTag.id) && Objects.equals(name, assetTag.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

}
