package com.cumulus.modules.business.gather.entity.mysql;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

/**
 * 采集状态
 *
 * @author zhangxq
 */
@Setter
@Getter
@Entity
@Table(name = "tbl_gather_task_status")
@DynamicUpdate
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GatherTaskStatus implements Serializable {

    private static final long serialVersionUID = -2445487619842891362L;

    /**
     * 任务id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 资产id
     */
    private Integer assetId;

    /**
     *  采集类型 json
     */
    private String gatherType;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

}
