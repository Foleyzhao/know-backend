package com.cumulus.modules.business.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.cumulus.base.BaseDTO;
import com.cumulus.modules.business.entity.AssetType;
import lombok.Getter;
import lombok.Setter;

/**
 * 资产类型
 *
 * @author zhangxq
 */
@Getter
@Setter
public class AssetSysTypeDto extends BaseDTO implements Serializable {

    private static final long serialVersionUID = -5028451223562429644L;

    /**
     * ID
     */
    private Integer id;

    /**
     * 类型名称
     */
    private String name;

    /**
     * 备注
     */
    private String description;

    /**
     * 是否内置
     */
    private boolean customize;

    /**
     * 编号
     */
    private String number;

    /**
     * 父资产类型
     */
    private AssetType assetType;

    /**
     * 父类型名称
     */
    private String parent;

    /**
     * 编号
     */
    private String parentNumber;

    /**
     * 录入结果
     */
    private String result;

    /**
     * 页面展示子类型
     */
    private List<AssetSysTypeDto> childrenType = new ArrayList();
}
