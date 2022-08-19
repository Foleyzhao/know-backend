package com.cumulus.modules.business.gather.entity.es;

import com.cumulus.modules.business.gather.request.GatherItemLog;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 资产采集项的采集日志
 *
 * @author zhaoff
 */
@Getter
@Setter
@Document(indexName = "gather-item-log")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GatherItemLogEs implements Serializable {

    private static final long serialVersionUID = -6766815471908956868L;

    /**
     * ID
     */
    @Id
    @Field(type = FieldType.Long)
    private Long id;

    /**
     * 执行结果状态（0 成功，-1失败）
     */

    @Field(type = FieldType.Integer)
    private Integer result;

    /**
     * 失败原因
     */
    @Field(type = FieldType.Keyword)
    private String reason;

    /**
     * 巡检项itemkey
     */
    @Field(type = FieldType.Keyword)
    private String itemKey;

    /**
     * 巡检项itemName
     */
    @Field(type = FieldType.Keyword)
    private String itemName;

    /**
     * 任务类型
     */
    @Field(type = FieldType.Keyword)
    private String taskType;

    /**
     * 采集id
     */
    @Field(type = FieldType.Keyword)
    private String gatherId;

    /**
     * 加入系统时间
     */
    @Field(type = FieldType.Date)
    private Date time;

    /**
     * 提取的结果变量
     */
    @Field(type = FieldType.Object)
    private Map<String, Object> elite;

    /**
     * 原始输出
     */
    @Field(type = FieldType.Object)
    private Map<String, Object> output;

    /**
     * 默认构造方法
     */
    public GatherItemLogEs() {
    }

    /**
     * 构造方法
     *
     * @param itemLog ES存储实例
     */
    public GatherItemLogEs(GatherItemLog itemLog) {
        this.setResult(itemLog.getResult());
        this.setElite(itemLog.getElite());
        this.setOutput(itemLog.getOutput());
        this.setReason(itemLog.getReason());
        this.setItemKey(itemLog.getItemKey());
        this.setItemName(itemLog.getItemName());
        this.setTaskType(itemLog.getTaskType());
        this.setGatherId(itemLog.getGatherId());
    }

}
