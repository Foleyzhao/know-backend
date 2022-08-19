package com.cumulus.modules.business.detect.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import com.cumulus.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

/**
 * 发现任务实体类
 *
 * @author zhangxq
 */
@Setter
@Getter
@Entity
@Table(name = "tbl_detect_task")
@DynamicUpdate
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DetectTask extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 2172532615889196007L;

    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 任务名称
     */
    @Column(name = "detect_task_name")
    private String detectTaskName;

    /**
     * 任务类型
     */
    private Integer taskType;

    /**
     * 任务状态
     */
    private Integer taskStatus;

    /**
     * ip范围
     */
    private Integer ipRange;

    /**
     * ip范围为 部门时  部门id列表
     */
    private String deptList;

    /**
     * ip列表
     */
    private String ipList;

    /**
     * 端口范围
     */
    private Integer portRange;

    /**
     * 端口列表
     */
    private String portList;

    /**
     * ping扫描开关
     */
    private Integer pingSwitch;

    /**
     * UDP端口
     */
    @Column(name = "port_range_UDP")
    private Integer portRangeUDP;

    /**
     * UDP端口列表
     */
    @Column(name = "port_list_UDP")
    private String portListUDP;

    /**
     * 发包速率
     */
    private Integer sendSpeed;

    /**
     * 组件识别
     */
    private Integer componentIdentify;

    /**
     * 上次执行结果 -> 最新发现资产
     */
    private String lastResult;

    /**
     * 下次执行时间
     */
    private Timestamp nextTime;

    /**
     * 执行参数
     */
    private String executeParam;

    /**
     * 开始时间
     */
    private Timestamp startTime;

    /**
     * cron表达式
     */
    private String cron;

    /**
     * 执行次数
     */
    private Integer num;

    /**
     * 在线
     */
    private Long online;

    /**
     * 离线
     */
    private Long offline;

    /**
     * 是否手动取消
     */
    private Boolean cancel;

    public DetectTask() {
        this.setCancel(false);
    }
}
