package com.cumulus.modules.business.gather.vo;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.cumulus.modules.business.gather.common.constant.GatherConstants;

import lombok.Data;

/**
 * 资产画像的VO
 *
 * @author shijh
 */
@Data
public class AssetPortraitVo {

    /**
     * ID
     */
    private String id;

    /**
     * 资产ID
     */
    private Long assetId;

    /**
     * 资产名称
     */
    private String name;

    /**
     * 资产 ip
     */
    private String ip;

    /**
     * 资产系统类型ID
     */
    private Integer assetSysType;

    /**
     * 资产系统类型名称
     */
    private String assetSysTypeName;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 资产版本
     */
    private String version;

    /**
     * 风险状态(安全资产:0 低危资产:1 中危资产:2 高危资产:3) -1未采集 = 安全
     */
    private Integer riskLevel = 0;

    /**
     * 资产状态 和Asset的 assetStatus 不同 (未采集:0 下线:1 异常:2)
     */
    private Integer status = GatherConstants.GATHER_ASSET_STATUS_NOT_COLLECTED;

    /**
     * 资产数据是否拼装完成
     */
    private boolean integrated;

    /**
     * 资产采集时间
     */
    private Date utime;

    /**
     * 资产详情
     * {
     * "gatherAssetLog":xxx,
     * "xxx(esIndex)":xxx
     * }
     */
    private Map<String, Object> details = new HashMap<>();

    /**
     * 采集信息（为方便查询和获取信息，对一些信息进行提取）
     * {
     * "version": "xxx",
     * "ipv6": xxx
     * }
     */
    private Map<String, Object> gatherInfo = new HashMap<>();

    /**
     * uuid
     */
    private String uuid;

    /**
     * 端口
     */
    private String port;

    /**
     * 归属部门
     */
    private String dept;

    /**
     * 地理位置
     */
    private String location;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 发现时间 该资产的发现时间
     */
    private Date findTime;

    /**
     * 协议
     */
    private String protocol;

    /**
     * 网址
     */
    private String url;

    /**
     * 服务
     */
    private String service;

    /**
     * 登录账号
     */
    private String account;

    /**
     * 指纹信息
     */
    private String fingerprint;

    /**
     * 是否有开放端口（0-没有，1-有）
     */
    private Integer hasPort = 0;

    /**
     * 服务组件
     */
    private String serviceComponent;

    /**
     * 标题
     */
    private String headline;

    /**
     * 区分主机资产和应用资 (1-主机 2-应用)
     */
    private Integer diff;

    /**
     * 资产状态 (存活:0 下线:1 异常:2)
     */
    private Integer assetStatus;

    /**
     * 资产类型
     */
    private String assetType;

    /**
     * 资产标签
     */
    private List<String> assetTags;

    /**
     * 负责人
     */
    private String leader;

    /**
     * 资产类型合并 父 - 子
     */
    private String mergeAssetType;

    /**
     * 登陆采集标签头
     */
    private String loginGatherHeader;

    /**
     * 远程扫描标签头
     */
    private String remoteScanHeader;

    /**
     * tab页
     */
    private String tab;


}
