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
 * 远程扫描-中间件
 *
 * @author Shijh
 */
@Getter
@Setter
@Document(indexName = "scan-middleware")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ScanMiddlewareEs implements Serializable {

    private static final long serialVersionUID = 4974033841112955122L;

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
     * 采集资产ID
     */
    @Field(type = FieldType.Keyword)
    private String scanAssetId;

    /**
     * 名称
     */
    @Field(type = FieldType.Keyword)
    private String name;

    /**
     * 服务端口
     */
    @Field(type = FieldType.Integer)
    private Integer port;

    /**
     * 开发语言
     */
    @Field(type = FieldType.Keyword)
    private String devLanguage;

    /**
     * 应用程序
     */
    @Field(type = FieldType.Keyword)
    private String app;

    /**
     * 采集时间
     */
    @Field(type = FieldType.Date)
    private Date utime;

}
