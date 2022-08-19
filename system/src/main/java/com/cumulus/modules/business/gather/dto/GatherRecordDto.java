package com.cumulus.modules.business.gather.dto;

import java.io.Serializable;
import java.sql.Timestamp;
import com.cumulus.base.BaseDTO;
import com.cumulus.modules.business.gather.entity.mysql.GatherPlan;
import lombok.Getter;
import lombok.Setter;

/**
 * 采集记录数据传输对象
 *
 * @author zhangxq
 */
@Getter
@Setter
public class GatherRecordDto extends BaseDTO implements Serializable {

    private static final long serialVersionUID = -8002377937935751487L;

    /**
     * ID
     */
    private Long id;

    /**
     * 采集对象
     */
    private String ipList;

    /**
     * 开始时间
     */
    private Timestamp startTime;

    /**
     * 采集任务
     */
    private GatherPlan gatherPlan;

    /**
     * 采集结果
     */
    private Integer result;

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
}
