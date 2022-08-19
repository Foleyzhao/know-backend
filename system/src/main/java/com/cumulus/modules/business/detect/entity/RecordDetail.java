package com.cumulus.modules.business.detect.entity;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

/**
 * @author zhangxq
 */
@Setter
@Getter
@Entity
@Table(name = "tbl_detect_record_detail")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@DynamicUpdate
public class RecordDetail implements Serializable {

    private static final long serialVersionUID = 8938241477306488456L;

    private static final int STATUS_ONLINE = 1;
    private static final int STATUS_OFFLINE = 2;

    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 是否在线
     */
    private boolean online;

    /**
     * ip
     */
    private String ip;

    /**
     * 发现任务记录
     */
    @JSONField(serialize = false)
    @ManyToOne
    @JoinColumn(name = "record_id")
    private DetectRecord detectRecord;

}
