package com.cumulus.modules.quartz.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * 定时任务日志实体
 *
 * @author zhaoff
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "quartz_log")
public class QuartzLog implements Serializable {

    private static final long serialVersionUID = -6050125590121157444L;

    /**
     * ID
     */
    @Id
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
     * bean名称
     */
    private String beanName;

    /**
     * 方法名称
     */
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
     * 执行间隔
     */
    private Long period;

    /**
     * 状态（是否执行成功）
     */
    private Boolean isSuccess;

    /**
     * 异常详情
     */
    private String exceptionDetail;

    /**
     * 执行耗时
     */
    private Long time;

    /**
     * 创建时间
     */
    @CreationTimestamp
    private Timestamp createTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (null == o || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        QuartzLog quartzLog = (QuartzLog) o;
        return Objects.equals(id, quartzLog.id);
    }

    @Override
    public int hashCode() {
        return 0;
    }

}
