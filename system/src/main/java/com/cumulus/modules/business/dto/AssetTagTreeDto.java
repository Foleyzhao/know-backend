package com.cumulus.modules.business.dto;

import com.cumulus.base.BaseDTO;
import com.cumulus.modules.business.entity.AssetTag;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.io.Serializable;

/**
 * @author : shenjc
 */
@Getter
@Setter
public class AssetTagTreeDto extends BaseDTO implements Serializable {
    private static final long serialVersionUID = -3677950054293216480L;
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
    private Page<AssetTag> subTagPage;

    /**
     * 编号
     */
    private String number;
}
