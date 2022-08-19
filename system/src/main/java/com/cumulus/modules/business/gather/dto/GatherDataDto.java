package com.cumulus.modules.business.gather.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * 采集数据DTO
 *
 * @author Shijh
 */
@Getter
@Setter
public class GatherDataDto implements Serializable {

    /**
     * 采集对象
     */
    private String ip;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 采集任务名称
     */
    private String planName;

    /**
     * 采集结果
     */
    private Integer result;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 耗时项采集结果
     */
    private Integer resultTimeConsuming;

    /**
     * 实时项采集结果
     */
    private Integer resultTimeReal;

    /**
     * 不常变化项采集结果
     */
    private Integer resultNoChange;

    /**
     * 标识
     */
    private Long flag;

}
