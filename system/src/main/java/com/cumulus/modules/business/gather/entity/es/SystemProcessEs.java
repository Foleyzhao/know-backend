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
 * 系统进程
 *
 * @author shijh
 */
@Data
@Document(indexName = "system-processes")
public class SystemProcessEs implements Serializable, GatherInstance {

    private static final long serialVersionUID = 5345793670984444931L;

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
     * 执行用户
     */
    @Field(type = FieldType.Keyword)
    private String user;

    /**
     * 进程名
     */
    @Field(type = FieldType.Keyword)
    private String process;

    /**
     * 父进程
     */
    @Field(type = FieldType.Keyword)
    private String parentProcess;

    /**
     * 状态
     */
    @Field(type = FieldType.Keyword)
    private String state;

    /**
     * 开始时间
     */
    @Field(type = FieldType.Date)
    private Date startTime;

    /**
     * 执行时间
     */
    @Field(type = FieldType.Keyword)
    private String executeTime;

    /**
     * CPU占用率
     */
    @Field(type = FieldType.Keyword)
    private String cpu;

    /**
     * 内存占有率
     */
    @Field(type = FieldType.Keyword)
    private String mem;

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
