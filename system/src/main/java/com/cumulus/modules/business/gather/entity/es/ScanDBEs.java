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
 * 远程扫描-数据库
 *
 * @author Shijh
 */
@Getter
@Setter
@Document(indexName = "scan-db")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ScanDBEs implements Serializable {

    private static final long serialVersionUID = 7503647549323971311L;

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
    @Field(type = FieldType.Keyword)
    private Integer port;

    /**
     * 采集时间
     */
    @Field(type = FieldType.Date)
    private Date utime;

}
