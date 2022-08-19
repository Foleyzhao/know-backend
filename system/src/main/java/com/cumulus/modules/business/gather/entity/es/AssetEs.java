package com.cumulus.modules.business.gather.entity.es;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 资产
 *
 * @author shijh
 */
@Data
@Document(indexName = "asset")
public class AssetEs implements Serializable {

    private static final long serialVersionUID = 5590757440090470669L;

    // 安全资产
    public static final int SAFETY = 0;
    // 低危资产
    public static final int LOW_RISK = 1;
    // 中危资产
    public static final int MIDDLE_RISK = 2;
    // 高危资产
    public static final int HIGH_RISK = 3;
    // 存活资产
    public static final int SURVIVE = 0;
    // 下线资产
    public static final int DOWN_LINE = 1;
    // 异常资产
    public static final int ABNORMAL = 2;

    /**
     * ID
     */
    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    /**
     * IP
     */
    @Field(type = FieldType.Keyword)
    private String ip;

    /**
     * 端口
     */
    @Field(type = FieldType.Keyword)
    private String port;

    /**
     * uuid
     */
    @Field(type = FieldType.Keyword)
    private String uuid;

    /**
     * 资产名称
     */
    @Field(type = FieldType.Keyword)
    private String name;

    /**
     * 主机名
     */
    @Field(type = FieldType.Keyword)
    private String hostName;

    /**
     * 负责人
     */
    @Field(type = FieldType.Keyword)
    private String leader;

    /**
     * 风险状态(安全资产:0 低危资产:1 中危资产:2 高危资产:3)
     */
    @Field(type = FieldType.Integer)
    private Integer riskLevel;

    /**
     * 归属部门
     */
    @Field(type = FieldType.Keyword)
    private String dept;

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
     * 在线时长
     */
    @Field(type = FieldType.Keyword)
    private String onlineTime;

    /**
     * 资产标签
     */
    @Field(type = FieldType.Keyword)
    private String assetTags;

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
     * 最近探测时间
     */
    @Field(type = FieldType.Date)
    private Date detectTime;

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
     * 硬件信息
     */
    @Field(type = FieldType.Keyword)
    private String hardware;

    /**
     * 磁盘分区列表
     */
    @Field(type = FieldType.Keyword)
    private List<String> diskPartitions;

    /**
     * 账号列表
     */
    @Field(type = FieldType.Keyword)
    private List<String> accounts;

    /**
     * 已装软件列表
     */
    @Field(type = FieldType.Keyword)
    private List<String> softwares;

    /**
     * 网络配置列表
     */
    @Field(type = FieldType.Keyword)
    private List<String> networks;

    /**
     * 系统进程列表
     */
    @Field(type = FieldType.Keyword)
    private List<String> systemProcesses;

    /**
     * 服务列表
     */
    @Field(type = FieldType.Keyword)
    private List<String> services;

    /**
     * 端口列表
     */
    @Field(type = FieldType.Keyword)
    private List<String> ports;

    /**
     * 路由列表
     */
    @Field(type = FieldType.Keyword)
    private List<String> routes;

    /**
     * 环境变量列表
     */
    @Field(type = FieldType.Keyword)
    private List<String> environments;

    /**
     * 历史变更列表
     */
    @Field(type = FieldType.Keyword)
    private List<String> histories;

    /**
     * 性能信息列表
     */
    @Field(type = FieldType.Keyword)
    private List<String> performances;

    /**
     * 收集时间
     */
    @Field(type = FieldType.Date)
    private Date utime;

    /**
     * web地址
     */
    @Field(type = FieldType.Keyword)
    private String webAddress;

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
     * 异常类型
     */
    @Field(type = FieldType.Keyword)
    private String abnormalType;

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
     * 区分主机资产和应用资 (0-主机 1-应用)
     */
    @Field(type = FieldType.Integer)
    private Integer diff;

    @Field(store=true,type = FieldType.Keyword)
    private String groupLevel;

    @Field(store=true,type = FieldType.Keyword)
    private List<AssetEs> assetEs;

    /**
     * 转义
     *
     * @param num 风险 num
     * @return 结果
     */
    public static String getRiskLevel(Integer num) {
        if (SAFETY == num) {
            return "安全资产";
        } else if (LOW_RISK == num) {
            return "低危资产";
        } else if (MIDDLE_RISK == num) {
            return "中危资产";
        } else if (HIGH_RISK == num) {
            return "高危资产";
        }
        return null;
    }


}
