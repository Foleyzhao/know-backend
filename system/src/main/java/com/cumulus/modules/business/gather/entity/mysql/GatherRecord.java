package com.cumulus.modules.business.gather.entity.mysql;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 采集任务记录
 *
 * @author zhangxq
 */
@Setter
@Getter
@Entity
@Table(name = "tbl_gather_record")
@DynamicUpdate
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GatherRecord implements Serializable {

    private static final long serialVersionUID = -2445487619842891362L;

    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 开始时间
     */
    private Timestamp startTime;

    /**
     * 终止时间
     */
    private Timestamp endTime;

    /**
     * 任务id
     */
    @ManyToOne
    @JoinColumn(name = "gather_task_id")
    private GatherPlan gatherPlan;

    /**
     * 采集计划名称
     */
    private String gatherTaskName;

    /**
     * 采集对象
     */
    private String ipList;

    /**
     * 采集结果
     */
    private Integer result;

    /**
     * 耗时项采集结果
     */
    @Column(name = "result_time_consuming")
    private Integer resultTimeConsuming;

    /**
     * 实时项采集结果
     */
    @Column(name = "result_time_real")
    private Integer resultTimeReal;

    /**
     * 不常变化项采集结果
     */
    @Column(name = "result_no_change")
    private Integer resultNoChange;

}
