package com.cumulus.modules.business.dto;

import java.io.Serializable;
import com.cumulus.modules.business.entity.AssetExtend;
import lombok.Getter;
import lombok.Setter;

/**
 * 精简确认资产数据传输对象
 *
 * @author zhangxq
 */
@Getter
@Setter
public class SimpleAssetConfirmDto implements Serializable {

    private static final long serialVersionUID = 5387693698807786488L;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 扩展属性
     */
    private AssetExtend assetExtend;
}
