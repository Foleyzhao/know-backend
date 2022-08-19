package com.cumulus.modules.business.gather.entity.es;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 采集结果
 *
 * @author Shijh
 */
@Data
@Document(indexName = "gather-result")
public class GatherResultEs implements Serializable {

    private static final long serialVersionUID = 3890746704007664315L;

    /**
     * 采集资产IP
     */
    @Id
    @Field(type = FieldType.Keyword)
    private String assetIp;

    /**
     * 采集计划ID
     */
    @Field(type = FieldType.Long)
    private Long planId;

    /**
     * 计划
     */
    @Field(type = FieldType.Keyword)
    private String planName;

    /**
     * 名称
     */
    @Field(type = FieldType.Keyword)
    private String name;

    /**
     * 结果
     */
    @Field(type = FieldType.Keyword)
    private Integer result;

    /**
     * 实时
     */
    @Field(type = FieldType.Keyword)
    private Integer frequently;

    /**
     * 耗时
     */
    @Field(type = FieldType.Keyword)
    private Integer stationary;

    /**
     * 不常变化
     */
    @Field(type = FieldType.Keyword)
    private Integer seldom;

    /**
     * 执行时间
     */
    @Field(type = FieldType.Date)
    private Date begin;

    /**
     * 执行结束时间
     */
    @Field(type = FieldType.Date)
    private Date end;

    /**
     * 采集对象
     */
    @Field(type = FieldType.Integer)
    private Integer gatherObj;

}
