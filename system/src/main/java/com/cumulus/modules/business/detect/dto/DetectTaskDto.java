package com.cumulus.modules.business.detect.dto;

import java.io.Serializable;
import java.sql.Timestamp;
import com.cumulus.base.BaseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * 发现任务数据传输对象
 *
 * @author zhangxq
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DetectTaskDto extends BaseDTO implements Serializable {

    private static final long serialVersionUID = 5688330497575103809L;

    /**
     * ID
     */
    private Long id;

    /**
     * 任务名称
     */
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
    private Integer portRangeUDP;

    /**
     * UDP端口列表
     */
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
     * 上次执行结果
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
     * 百分比
     */
    private Integer percentage;

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

    /**
     * 任务进度 20/100
     */
    private String progress;

}
