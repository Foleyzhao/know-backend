package com.cumulus.modules.business.gather.entity.es;

import com.cumulus.modules.business.gather.common.constant.GatherConstants;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 采集资产
 *
 * @author zhaoff
 */
@Getter
@Setter
@Document(indexName = "gather-asset")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GatherAssetEs implements Serializable {

    private static final long serialVersionUID = 4633301890630279597L;

    /**
     * 采集信息-版本
     */
    public static final String GATHER_INFO_VERSION = "version";

    /**
     * 采集信息-是否是IPV6
     */
    public static final String GATHER_INFO_IPV6 = "ipv6";

    /**
     * 采集信息-版本号
     */
    public static final String GATHER_INFO_VERSION_NUMBER = "versionNumber";

    /**
     * 资产详情-任务类型与资产采集日志映射
     */
    public static final String DETAILS_ASSET_LOG = "gatherAssetLog";

    /**
     * 资产详情-采集项ID
     */
    public static final String DETAILS_ITEM_LOG_IDS = "ids";

    /**
     * 资产详情-采集项风险
     */
    public static final String DETAILS_ITEM_LOG_LEVEL = "level";

    /**
     * 资产详情-采集项标识
     */
    public static final String DETAILS_ITEM_LOG_FLAG = "flag";

    /**
     * 资产详情-采集项采集时间
     */
    public static final String DETAILS_ITEM_LOG_UTIME = "utime";

    /**
     * ID
     */
    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    /**
     * 资产ID
     */
    @Field(type = FieldType.Long)
    private Long assetId;

    /**
     * 资产名称
     */
    @Field(type = FieldType.Keyword)
    private String name;

    /**
     * 资产 ip
     */
    @Field(type = FieldType.Keyword)
    private String ip;

    /**
     * 资产系统类型ID
     */
    @Field(type = FieldType.Integer)
    private Integer assetSysType;

    /**
     * 资产系统类型名称
     */
    @Field(type = FieldType.Keyword)
    private String assetSysTypeName;

    /**
     * 任务类型
     */
    @Field(type = FieldType.Integer)
    private String taskType;

    /**
     * 资产版本
     */
    @Field(type = FieldType.Keyword)
    private String version;

    /**
     * 风险状态(安全资产:0 低危资产:1 中危资产:2 高危资产:3) -1未采集 = 安全
     */
    @Field(type = FieldType.Integer)
    private Integer riskLevel = 0;

    /**
     * 资产状态 和Asset的 assetStatus 不同 (未采集:0 下线:1 异常:2)
     */
    @Field(type = FieldType.Integer)
    private Integer status = GatherConstants.GATHER_ASSET_STATUS_NOT_COLLECTED;

    /**
     * 资产数据是否拼装完成
     */
    @Field(type = FieldType.Boolean)
    private boolean integrated;

    /**
     * 资产采集时间
     */
    @Field(type = FieldType.Date)
    private Date utime;

    /**
     * 资产详情
     * {
     * "gatherAssetLog":xxx,
     * "xxx(esIndex)":xxx
     * }
     */
    @Field(type = FieldType.Object)
    private Map<String, Object> details = new HashMap<>();

    /**
     * 采集信息（为方便查询和获取信息，对一些信息进行提取）
     * {
     * "version": "xxx",
     * "ipv6": xxx
     * }
     */
    @Field(type = FieldType.Object)
    private Map<String, Object> gatherInfo = new HashMap<>();

    /**
     * uuid
     */
    @Field(type = FieldType.Keyword)
    private String uuid;

    /**
     * 端口
     */
    @Field(type = FieldType.Keyword)
    private String port;

    /**
     * 归属部门
     */
    @Field(type = FieldType.Keyword)
    private String dept;

    /**
     * 地理位置
     */
    @Field(type = FieldType.Keyword)
    private String location;

    /**
     * 更新时间
     */
    @Field(type = FieldType.Date)
    private Date updateTime;

    /**
     * 发现时间 该资产的发现时间
     */
    @Field(type = FieldType.Date)
    private Date findTime;

    /**
     * 协议
     */
    @Field(type = FieldType.Keyword)
    private String protocol;

    /**
     * 网址
     */
    @Field(type = FieldType.Keyword)
    private String url;

    /**
     * 服务
     */
    @Field(type = FieldType.Keyword)
    private String service;

    /**
     * 登录账号
     */
    @Field(type = FieldType.Keyword)
    private String account;

    /**
     * 指纹信息
     */
    @Field(type = FieldType.Keyword)
    private String fingerprint;

    /**
     * 是否有开放端口（0-没有，1-有）
     */
    @Field(type = FieldType.Integer)
    private Integer hasPort = 0;

    /**
     * 服务组件
     */
    @Field(type = FieldType.Keyword)
    private String serviceComponent;

    /**
     * 标题
     */
    @Field(type = FieldType.Keyword)
    private String headline;

    /**
     * 区分主机资产和应用资 (1-主机 2-应用)
     */
    @Field(type = FieldType.Integer)
    private Integer diff;

    /**
     * 资产状态 (存活:0 下线:1 异常:2)
     */
    @Field(type = FieldType.Integer)
    private Integer assetStatus;

    /**
     * 资产类型
     */
    @Field(type = FieldType.Keyword)
    private String assetType;

    /**
     * 资产标签
     */
    @Field(type = FieldType.Keyword)
    private List<String> assetTags;

    /**
     * 负责人
     */
    @Field(type = FieldType.Keyword)
    private String leader;

    /**
     * 资产类型合并 父 - 子
     */
    private String mergeAssetType;

}
