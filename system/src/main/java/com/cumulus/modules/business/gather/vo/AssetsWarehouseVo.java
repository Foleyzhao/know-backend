package com.cumulus.modules.business.gather.vo;

import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * 资产视图的Vo
 *
 * @author Shijh
 */
@Data
public class AssetsWarehouseVo {

    /**
     * 安全资产
     */
    private Integer safety;

    /**
     * 低危资产
     */
    private Integer lowRisk;

    /**
     * 中危资产
     */
    private Integer middleRisk;

    /**
     * 高危资产
     */
    private Integer highRisk;

    /**
     * 存活资产
     */
    private Integer survive;

    /**
     * 异常资产
     */
    private Integer abnormal;

    /**
     * 下线资产
     */
    private Integer downLine;

    /**
     * 全部资产资产
     */
    private Integer all;

    /**
     * ip
     */
    private String ip;

    /**
     * 端口
     */
    private String port;

    /**
     * 资产名称
     */
    private String name;

    /**
     * 资产类型
     */
    private List<String> assetType;

    /**
     * 资产标签
     */
    private List<String> assetTags;

    /**
     * 资产归属
     */
    private List<String> dept;

    /**
     * 资产状态(存活:0 下线:1 异常:2)
     */
    private List<Integer> assetStatus;

    /**
     * 异常类型
     */
    private List<String> abnormalType;

    /**
     * 风险等级(安全资产:0 低危资产:1 中危资产:2 高危资产:3)
     */
    private List<Integer> riskLevel;

    /**
     * web地址
     */
    private String webAddress;

    /**
     * 指纹信息
     */
    private String fingerprint;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 收集时间
     */
    private Date uTime;

    /**
     * 排序字段
     */
    private String field;

    /**
     * 排序规则，asc升序，desc降序
     */
    private String order;

    /**
     * 区分主机资产和应用资 (0-主机 1-应用)
     */
    private Integer diff;

    /**
     * 多个条件查询(ip/名称/port)
     */
    private String more;

}
