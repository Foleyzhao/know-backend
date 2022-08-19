package com.cumulus.modules.business.gather.entity.es;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 基本信息
 *
 * @author shenjc
 */
@Getter
@Setter
@Document(indexName = "basic-info")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class BasicInfoEs implements Serializable, GatherInstance {

    private static final long serialVersionUID = -7362813536377734915L;

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
     * 收集时间
     */
    @Field(type = FieldType.Date)
    private Date utime;

    /**
     * 运行时长 (启动了多久)
     */
    @Field(type = FieldType.Keyword)
    private String runTime;

    /**
     * uuid
     */
    @Field(type = FieldType.Keyword)
    private String uuid;

    /**
     * 主机名
     */
    @Field(type = FieldType.Keyword)
    private String hostname;

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
