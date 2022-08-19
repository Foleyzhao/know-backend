package com.cumulus.modules.business.gather.entity.es;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 远程扫描-基本信息
 *
 * @author shijh
 */
@Getter
@Setter
@Document(indexName = "scan-basic-info")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ScanBasicInfoEs implements Serializable {

    private static final long serialVersionUID = 7171075114253794656L;

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
     * 扫描资产ID
     */
    @Field(type = FieldType.Keyword)
    private String scanAssetId;

    /**
     * 域名
     */
    @Field(type = FieldType.Keyword)
    private String domainName;

    /**
     * 备案信息
     */
    @Field(type = FieldType.Keyword)
    private String records;

    /**
     * 最近探测时间
     */
    @Field(type = FieldType.Date)
    private Date detectTime;

    /**
     * 地理位置
     */
    @Field(type = FieldType.Keyword)
    private String location;

    /**
     * 操作系统
     */
    @Field(type = FieldType.Keyword)
    private String operatingSystem;

    /**
     * 采集时间
     */
    @Field(type = FieldType.Date)
    private Date utime;

    /**
     * 详情
     */
    @Field(type = FieldType.Object)
    private Map<String, Object> detail;

}
