package com.cumulus.modules.business.gather.entity.es;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 数据库实体
 *
 * @author shijh
 */
@Getter
@Setter
@Document(indexName = "db")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DBEs implements Serializable {

    private static final long serialVersionUID = 289522134051133206L;

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
     * 类型
     */
    @Field(type = FieldType.Keyword)
    private String type;

    /**
     * 版本
     */
    @Field(type = FieldType.Keyword)
    private String version;

    /**
     * 服务端口
     */
    @Field(type = FieldType.Integer)
    private Integer port;

    /**
     * 启动用户
     */
    @Field(type = FieldType.Keyword)
    private String user;

    /**
     * 采集时间
     */
    @Field(type = FieldType.Date)
    private Date utime;

}
