package com.cumulus.modules.business.gather.entity.mysql;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * 采集周期实体类
 *
 * @author zhaoff
 */
@Getter
@Setter
@Entity
@Table(name = "tbl_gather_period")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GatherPeriod implements Serializable {

    private static final long serialVersionUID = 2585478440977766917L;

    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 采集内容（frequently-实时，stationary-耗时，seldom-不常变化）
     */
    private String content;

    /**
     * 开始执行时间
     */
    private Timestamp startTime;

    /**
     * 执行间隔
     */
    private Integer period;

    /**
     * 执行间隔单位（day、month、hour、minute、once）
     */
    private String unit;

    /**
     * 执行cron表达式
     */
    private String cron;

    /**
     * 采集计划
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gather_plan_id")
    @JSONField(serialize = false)
    private GatherPlan gatherPlan;

    /**
     * 复制属性
     *
     * @param period 采集周期
     * @return 新采集周期
     */
    public static GatherPeriod copy(GatherPeriod period) {
        if (period == null) {
            return null;
        }
        GatherPeriod newPeriod = new GatherPeriod();
        newPeriod.setPeriod(period.getPeriod());
        newPeriod.setGatherPlan(period.getGatherPlan());
        newPeriod.setUnit(period.getUnit());
        newPeriod.setStartTime(period.getStartTime());
        newPeriod.setCron(period.getCron());
        return newPeriod;
    }
}
