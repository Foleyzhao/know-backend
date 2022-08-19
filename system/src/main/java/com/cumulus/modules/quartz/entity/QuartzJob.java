package com.cumulus.modules.quartz.entity;

import com.cumulus.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 定时任务实体
 *
 * @author zhaoff
 */
@Getter
@Setter
@Entity
@Table(name = "quartz_job")
public class QuartzJob extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -7380075709217540662L;

    /**
     * Quartz 定时任务名前缀
     */
    public static final String JOB_KEY = "JOB_KEY";

    /**
     * ID
     */
    @Id
    @NotNull(groups = {Update.class})
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 任务名称
     */
    private String jobName;

    /**
     * 任务类型
     */
    private String jobType;

    /**
     * Bean名称
     */
    @NotBlank
    private String beanName;

    /**
     * 方法名称
     */
    @NotBlank
    private String methodName;

    /**
     * 参数
     */
    private Long params;

    /**
     * 开始时间
     */
    private Timestamp startTime;

    /**
     * cron表达式
     */
    private String cronExpression;

    /**
     * 执行间隔（单位秒）
     */
    private Long period;

    /**
     * 状态（是否暂停）
     */
    private Boolean isPause = false;

    /**
     * 描述
     */
    private String description;

}
