package com.cumulus.modules.business.dto;

import com.cumulus.base.BaseEntity;
import com.cumulus.modules.business.entity.Asset;
import com.cumulus.modules.business.entity.AssetExtend;
import com.cumulus.modules.business.entity.AssetSysType;
import com.cumulus.modules.business.entity.AssetTag;
import com.cumulus.modules.business.entity.AssetType;
import com.cumulus.modules.system.entity.Dept;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.Set;

/**
 * 资产仓库Dto
 *
 * @author : shenjc
 */
@Getter
@Setter
public class AssetWarehouseDto extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -2626211355169185942L;

    /**
     * ID
     */
    private Long id;

    /**
     * IP
     */
    private String ip;

    /**
     * 资产名称
     */
    private String name;

    /**
     * IP全写
     */
    private String completeIp;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 资产类型
     */
    private AssetType assetType;

    /**
     * 资产系统类型
     */
    private AssetSysType assetSysType;

    /**
     * 资产类别（主机，应用）
     */
    private Integer assetCategory;

    /**
     * 所属部门
     */
    private Dept dept;

    /**
     * 协议
     */
    private String protocol;

    /**
     * 账号
     */
    private String account;

    /**
     * 密码
     */
    private String pwd;

    /**
     * 资产编码类型（ISO-8859-1/GB18030/US-ASCII/UTF-8）
     */
    private String charset;

    /**
     * 资产扩展属性
     */
    private AssetExtend assetExtend;

    /**
     * 资产状态
     */
    private Integer assetStatus;

    /**
     * 风险等级
     */
    private Integer riskLevel;

    /**
     * 在线时长
     */
    private Integer onlineTime;

    /**
     * agent状态
     */
    private Integer agentStatus;

    /**
     * 资产标签
     */
    private Set<AssetTag> assetTags;

    /**
     * 父资产
     */
    private Asset parent;

    /**
     * web地址
     */
    private String webAddress;

    /**
     * 指纹信息
     */
    private String fingerprint;

    /**
     * 资产风险包括子资产 0-1 前面是父资产风险 后面是子资产风险
     */
    private String allRiskLevel;

    /**
     * 子资产列表分页
     */
    private Page<Asset> childAssets;

    /**
     * ES的扫描资产id 空代表没有扫描过
     */
    private String gatherAssetId;

    /**
     * 资产类型合并 父 - 子
     */
    private String mergeAssetType;

}
