package com.cumulus.modules.business.gather.entity.es;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 路由
 *
 * @author Shijh
 */
@Data
@Document(indexName = "route")
public class RouteEs implements Serializable, GatherInstance {

    private static final long serialVersionUID = -6932917022066705276L;

    /**
     * ID
     */
    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    /**
     * 采集资产ID
     */
    @Field(type = FieldType.Keyword)
    private String gatherAssetId;

    /**
     * 资产ID
     */
    @Field(type = FieldType.Long)
    private Long assetId;

    /**
     * 目标地址
     */
    @Field(type = FieldType.Keyword)
    private String target;

    /**
     * 输出接口
     */
    @Field(type = FieldType.Keyword)
    private String ifs;

    /**
     * 网关地址
     */
    @Field(type = FieldType.Keyword)
    private String gateway;

    /**
     * 子网掩码
     */
    @Field(type = FieldType.Keyword)
    private String mask;

    /**
     * 收集时间
     */
    @Field(type = FieldType.Date)
    private Date utime;

    /**
     * 风险类型
     */
    @Field(type = FieldType.Object)
    private List<Long> riskType;

    /**
     * 风险等级
     */
    @Field(type = FieldType.Integer)
    private Integer riskLevel;

    /**
     * 详情
     */
    @Field(type = FieldType.Object)
    private Map<String, Object> detail;
}
