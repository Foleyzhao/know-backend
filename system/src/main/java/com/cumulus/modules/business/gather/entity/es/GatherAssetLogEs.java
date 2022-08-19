package com.cumulus.modules.business.gather.entity.es;

import com.cumulus.modules.business.gather.common.constant.GatherConstants;
import com.cumulus.modules.business.gather.request.GatherItemLog;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 资产采集日志
 *
 * @author zhaoff
 */
@Getter
@Setter
@Document(indexName = "gather-asset-log")
public class GatherAssetLogEs implements Serializable {

    private static final long serialVersionUID = -219982949322231361L;

    /**
     * 采集日志ID
     */
    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    /**
     * 采集ID
     */
    @Field(type = FieldType.Keyword)
    private String gatherId;

    /**
     * 任务类型
     */
    @Field(type = FieldType.Keyword)
    private String taskType;

    /**
     * 采集任务日志ID
     */
    @Field(type = FieldType.Keyword)
    private String taskLogId;

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
     * 采集资产ID
     */
    @Field(type = FieldType.Long)
    private Long assetId;

    /**
     * 采集资产名称
     */
    @Field(type = FieldType.Keyword)
    private String assetName;

    /**
     * 采集资产IP
     */
    @Field(type = FieldType.Keyword)
    private String assetIp;

    /**
     * 执行开始时间
     */
    @Field(type = FieldType.Date)
    private Date begin;

    /**
     * 执行结束时间
     */
    @Field(type = FieldType.Date)
    private Date end;

    /**
     * 任务执行状态
     */
    @Field(type = FieldType.Integer)
    private Integer state = GatherConstants.STATE_UNSTART;

    /**
     * 采集项数目
     */
    @Field(type = FieldType.Integer)
    private Integer size = 0;

    /**
     * 本次采集的采集项key列表
     */
    @Field(type = FieldType.Keyword)
    private Set<String> itemKeys = new HashSet<>();

    /**
     * 任务创建时间，尚未执行
     */
    @Field(type = FieldType.Long)
    private Long create;

    /**
     * 执行结果状态，0 成功,1 失败,2 部分成功
     */
    @Field(type = FieldType.Integer)
    private Integer result;

    /**
     * 失败原因
     */
    @Field(type = FieldType.Keyword)
    private String reason;

    /**
     * 异常数
     */
    @Field(type = FieldType.Integer)
    private Integer abnormalSize;

    /**
     * 单个数据采集项
     */
    @Field(type = FieldType.Object)
    private CopyOnWriteArrayList<GatherItemLog> itemlogs = new CopyOnWriteArrayList<>();

    /**
     * 类型 0-主机 1-网络 2-数据库 3-应用系统 5-安全 6-中间件
     */
    @Transient
    private int assetType;

    /**
     * 标识同一个任务
     */
    @Field(type = FieldType.Long)
    private Long flag;

    /**
     * 资产版本
     */
    @Field(type = FieldType.Keyword)
    private String version;

    /**
     * 是否使用agent采集
     */
    @Field(type = FieldType.Boolean)
    private boolean useAgent;

    /**
     * 增加检查项日志
     *
     * @param itemLog 检查项
     */
    public void addItemLog(GatherItemLog itemLog) {
        itemlogs.add(itemLog);
    }

}
