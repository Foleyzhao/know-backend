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
 * 远程扫描-端口
 *
 * @author Shijh
 */
@Getter
@Setter
@Document(indexName = "scan-service-port")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ScanServicePortEs implements Serializable {

    private static final long serialVersionUID = 3911632815654944450L;

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
    private String scanAssetId;

    /**
     * 资产ID
     */
    @Field(type = FieldType.Long)
    private Long assetId;

    /**
     * 端口
     */
    @Field(type = FieldType.Integer)
    private Integer port;

    /**
     * 状态
     */
    @Field(type = FieldType.Keyword)
    private String state;

    /**
     * 协议
     */
    @Field(type = FieldType.Keyword)
    private String protocol;

    /**
     * 产品名称
     */
    @Field(type = FieldType.Keyword)
    private String productName;

    /**
     * 版本号
     */
    @Field(type = FieldType.Keyword)
    private String version;

    /**
     * 收集时间
     */
    @Field(type = FieldType.Date)
    private Date utime;

}
