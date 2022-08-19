package com.cumulus.modules.business.dto;

import java.io.Serializable;
import java.util.List;
import com.cumulus.base.BaseDTO;
import com.cumulus.modules.business.entity.AssetSysType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

/**
 * 资产类型传输对象
 *
 * @author zhangxq
 */
@Getter
@Setter
public class AssetTypeDto extends BaseDTO implements Serializable {

    private static final long serialVersionUID = -7046635791542725824L;

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
    private Boolean customize;

    /**
     * 编号
     */
    private String number;

    /**
     * 子类型
     */
    private Page<AssetSysType> childrenType;

    /**
     * 录入结果
     */
    private String result;
}
