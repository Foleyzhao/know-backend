package com.cumulus.modules.business.other.entity.es;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 异常表
 *
 * @author Shijh
 */
@Data
@Document(indexName = "abnormal")
public class AbnormalEs implements Serializable {

    private static final long serialVersionUID = 2819927406948214150L;

    /**
     * ID
     */
    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    /**
     * 资产IP
     */
    @Field(type = FieldType.Keyword)
    private String ip;

    /**
     * 资产名称
     */
    @Field(type = FieldType.Keyword)
    private String name;

    /**
     * 资产标签
     */
    @Field(type = FieldType.Keyword)
    private String tags;

    /**
     * 资产类型
     */
    @Field(type = FieldType.Keyword)
    private String type;

    /**
     * 资产归属
     */
    @Field(type = FieldType.Keyword)
    private String dept;

    /**
     * 告警日期
     */
    @Field(type = FieldType.Date)
    private Date warningTime;

    /**
     * 采集时间
     */
    @Field(type = FieldType.Date)
    private Date utime;
}
