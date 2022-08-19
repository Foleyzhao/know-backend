package com.cumulus.modules.business.gather.entity.mysql;

import com.cumulus.base.BaseEntity;
import com.cumulus.modules.business.entity.Asset;
import com.cumulus.modules.business.gather.common.constant.GatherConstants;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * 采集任务实体类
 *
 * @author zhangxq
 */
@Setter
@Getter
@Entity
@Table(name = "tbl_gather_plan")
@DynamicUpdate
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GatherPlan extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 5383069917817588104L;

    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 计划名称
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

    @OneToMany(mappedBy = "gatherPlan", fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private List<GatherPeriod> gatherPeriods;

    /**
     * 采集资产列表
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "tbl_plan_asset",
            joinColumns = {@JoinColumn(name = "plan_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "asset_id", referencedColumnName = "id")})
    private List<Asset> assetList;

    /**
     * 正在执行的采集任务数量
     */
    @Column(name = "task_running_num")
    private Integer taskRunningNum;

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

}
