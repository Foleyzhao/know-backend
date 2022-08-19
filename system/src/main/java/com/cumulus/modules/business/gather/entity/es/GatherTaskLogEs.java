package com.cumulus.modules.business.gather.entity.es;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 采集任务日志
 *
 * @author zhaoff
 */
@Getter
@Setter
@Document(indexName = "gather-task-log")
public class GatherTaskLogEs implements Serializable {

    private static final long serialVersionUID = -8117745330534209716L;

    /**
     * ID
     */
    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    /**
     * 采集计划ID
     */
    @Field(type = FieldType.Long)
    private Long planId;

    /**
     * 采集计划名称
     */
    @Field(type = FieldType.Keyword)
    private String planName;

    /**
     * 任务类型（实时、耗时、不常变化）
     */
    @Field(type = FieldType.Keyword)
    private String taskType;

    /**
     * 执行方式（自动，手动）
     */
    @Field(type = FieldType.Integer)
    private Integer execType;

    /**
     * 开始执行时间
     */
    @Field(type = FieldType.Date)
    private Date begin;

    /**
     * 结束执行时间
     */
    @Field(type = FieldType.Date)
    private Date end;

    /**
     * 执行者ID
     */
    @Field(type = FieldType.Integer)
    private Long executorId;

    /**
     * 执行者名称
     */
    @Field(type = FieldType.Keyword)
    private String executorName;

    /**
     * 任务状态（0-成功，1-失败，2-部分成功，3-正在进行中，4-可终止状态，5-终止中）
     */
    @Field(type = FieldType.Integer)
    private Integer taskState;

    /**
     * 采集资产数量
     */
    @Field(type = FieldType.Integer)
    private Integer assetSize = 0;

    /**
     * 采集失败原因
     */
    @Field(type = FieldType.Keyword)
    private String reason;

    /**
     * 标识同一个任务
     */
    @Field(type = FieldType.Keyword)
    private String flag;

    /**
     * 采集结果
     * {
     * "succeed":10,
     * "portion":10,
     * "failed":10
     * }
     **/
    @Field(type = FieldType.Object)
    private Map<String, Object> taskResult = new HashMap<>();

}
