package com.cumulus.modules.business.gather.request;

import com.cumulus.modules.business.gather.entity.es.GatherItemLogEs;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

/**
 * 采集项日志
 *
 * @author zhaoff
 */
@Getter
@Setter
public class GatherItemLog implements Serializable {

    private static final long serialVersionUID = -1757585003106796553L;

    /**
     * 采集ID
     */
    private String gatherId;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 采集项key
     */
    private String itemKey;

    /**
     * 采集项名
     */
    private String itemName;

    /**
     * 执行结果状态（0 成功，1失败）
     */
    private Integer result;

    /**
     * 提取的结果变量
     */
    private Map<String, Object> elite;

    /**
     * 原始输出
     */
    private Map<String, Object> output;

    /**
     * 失败原因
     */
    private String reason;

    public GatherItemLog() {
    }

    /**
     * 构造方法
     *
     * @param entity 采集项日志实体
     */
    public GatherItemLog(GatherItemLogEs entity) {
        this.setGatherId(entity.getGatherId());
        this.setTaskType(entity.getTaskType());
        this.setItemKey(entity.getItemKey());
        this.setItemName(entity.getItemName());
        this.setResult(entity.getResult());
        this.setElite(entity.getElite());
        this.setOutput(entity.getOutput());
        this.setReason(entity.getReason());
    }
}
