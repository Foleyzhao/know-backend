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
 * 已装软件
 *
 * @author shijh
 */
@Data
@Document(indexName = "software")
public class SoftwareEs implements Serializable, GatherInstance {

    private static final long serialVersionUID = -5622190379529367204L;

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
     * 名称
     */
    @Field(type = FieldType.Keyword)
    private String name;

    /**
     * 版本
     */
    @Field(type = FieldType.Keyword)
    private String version;

    /**
     * 厂商
     */
    @Field(type = FieldType.Keyword)
    private String company;

    /**
     * 创建时间/安装时间
     */
    @Field(type = FieldType.Keyword)
    private String createTime;

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
