package com.cumulus.modules.business.detect.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

/**
 * 发现任务记录实体类
 *
 * @author zhangxq
 */
@Setter
@Getter
@Entity
@Table(name = "tbl_detect_record")
@DynamicUpdate
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DetectRecord implements Serializable {

    private static final long serialVersionUID = 2920175595277288179L;

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
     * 目标网段
     */
    private String ipList;

    /**
     * 任务ID
     */
    @Column(name = "detect_task_id")
    private Long detectTaskId;

    /**
     * 任务结果
     */
    private String result;

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
    private boolean cancel = false;

    /**
     * 任务结果
     */
    @JSONField(serialize = false)
    @OneToMany(mappedBy = "detectRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RecordDetail> detectResult;

    public DetectRecord() {
        this.setCancel(false);
    }
}
