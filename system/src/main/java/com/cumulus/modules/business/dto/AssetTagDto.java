package com.cumulus.modules.business.dto;

import java.io.Serializable;
import com.cumulus.base.BaseDTO;
import lombok.Getter;
import lombok.Setter;

/**
 * 资产标签数据传输对象
 *
 * @author zhangxq
 */
@Getter
@Setter
public class AssetTagDto extends BaseDTO implements Serializable {

    private static final long serialVersionUID = -1302234668237750466L;

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
    private boolean enabled;

    /**
     * 是否是内置标签
     */
    private boolean customize;

    /**
     * 备注
     */
    private String description;

    /**
     * 编号
     */
    private String number;

    /**
     * 录入结果
     */
    private String result;
}
