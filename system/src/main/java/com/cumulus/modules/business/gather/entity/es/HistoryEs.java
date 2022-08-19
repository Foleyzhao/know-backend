package com.cumulus.modules.business.gather.entity.es;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 历史变更
 *
 * @author Shijh
 */
@Data
@Document(indexName = "history")
public class HistoryEs implements Serializable {

    private static final long serialVersionUID = -4720545911280128712L;

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
    @Field(type = FieldType.Keyword)
    private String assetId;

    /**
     * 来源 create | update | delete
     */
    @Field(type = FieldType.Keyword)
    private String source;

    /**
     * 来源 如: Agent 采集 用户
     */
    @Field(type = FieldType.Keyword)
    private String dateSource;

    /**
     * 变更后
     */
    @Field(type = FieldType.Keyword)
    private String after;

    /**
     * 变更前
     */
    @Field(type = FieldType.Keyword)
    private String before;

    /**
     * 变更项
     */
    @Field(type = FieldType.Keyword)
    private String item;

    /**
     * 资产类别 1主机 2应用
     */
    @Field(type = FieldType.Keyword)
    private Integer assetCategory;

    /**
     * 变更时间
     */
    @Field(type = FieldType.Date)
    private Date updateTime;

    /**
     * 收集时间
     */
    @Field(type = FieldType.Date)
    private Date utime;

}
