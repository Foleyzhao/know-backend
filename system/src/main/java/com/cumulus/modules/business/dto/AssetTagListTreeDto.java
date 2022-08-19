package com.cumulus.modules.business.dto;

import com.cumulus.base.BaseDTO;
import com.cumulus.modules.business.entity.AssetTag;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author : shenjc
 */
@Getter
@Setter
public class AssetTagListTreeDto extends BaseDTO implements Serializable {
    private static final long serialVersionUID = 1976954491500388371L;
    /**
     * ID
     */
    private Long id;

    /**
     * 类型名称
     */
    private String name;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 是否是内置标签
     */
    private Boolean customize;

    /**
     * 备注
     */
    private String description;

    /**
     * 子资产列表
     */
    private List<AssetTag> subTagList;

    /**
     * 编号
     */
    private String number;
}
