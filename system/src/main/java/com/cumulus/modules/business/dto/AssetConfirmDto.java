package com.cumulus.modules.business.dto;

import java.io.Serializable;
import com.cumulus.base.BaseDTO;
import com.cumulus.modules.business.detect.entity.DetectTask;
import com.cumulus.modules.business.entity.AssetExtend;
import com.cumulus.modules.business.entity.AssetSysType;
import com.cumulus.modules.business.entity.AssetType;
import com.cumulus.modules.system.entity.Dept;
import lombok.Getter;
import lombok.Setter;

/**
 * 确认资产数据传输对象
 *
 * @author zhangxq
 */
@Getter
@Setter
public class AssetConfirmDto extends BaseDTO implements Serializable {

    private static final long serialVersionUID = 2524215670324021125L;

    /**
     * ID
     */
    private Long id;

    /**
     * ip
     */
    private String ip;

    /**
     * ip全写
     */
    private String completeIp;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 开放端口
     */
    private String openPort;

    /**
     * 资产类型
     */
    private AssetType assetType;

    /**
     * 子资产类型
     */
    private AssetSysType assetSysType;

    /**
     * 资产类别 主机 应用
     */
    private Integer assetCategory;

    /**
     * 协议
     */
    private String protocol;

    /**
     * 发现任务
     */
    private DetectTask detectTask;

    /**
     * 服务
     */
    private String server;

    /**
     * 网址
     */
    private String website;

    /**
     * 采集方式 1-Agent 2-登录采集 3-无访问
     */
    private Integer gatherType;

    /**
     * 是否在线
     */
    private Boolean online;

    /**
     * 扩展属性
     */
    private AssetExtend assetExtend;

    /**
     * 所属部门
     */
    private Dept dept;

}
