package com.cumulus.modules.business.gather.dto;

import com.cumulus.base.BaseDTO;
import com.cumulus.modules.business.entity.Asset;
import com.cumulus.modules.business.gather.common.constant.GatherConstants;
import com.cumulus.modules.business.gather.entity.mysql.GatherPeriod;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * 采集任务数据传输对象
 *
 * @author zhangxq
 */
@Getter
@Setter
public class GatherTaskDto extends BaseDTO implements Serializable {

    private static final long serialVersionUID = 3064149805718952736L;

    /**
     * ID
     */
    private Long id;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 执行方式（1-手动执行，2-自动执行）
     */
    private Integer execution;

    /**
     * 计划状态（0-未开始，1-正在执行，2-执行结束，3-正在取消，4-暂停）
     */
    private Integer status = GatherConstants.STATE_UNSTART;

    /**
     * 采集执行周期
     */
    private List<GatherPeriod> gatherPeriods;

    /**
     * 采集对象
     */
    private Integer gatherObj;

    /**
     * 采集对象数量
     */
    private Integer gatherNum;

    /**
     * 采集对象 部门时  部门id列表
     */
    private String deptList;

    /**
     * 采集资产列表
     */
    private List<Asset> assetList;

    /**
     * 执行参数
     */
    private String executeParam;

    /**
     * 开始时间
     */
    private Timestamp startTime;
}
