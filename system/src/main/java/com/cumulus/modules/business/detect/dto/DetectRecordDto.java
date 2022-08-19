package com.cumulus.modules.business.detect.dto;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import com.cumulus.base.BaseDTO;
import lombok.Getter;
import lombok.Setter;

/**
 * 发现记录数据传输对象
 *
 * @author zhangxq
 */
@Getter
@Setter
public class DetectRecordDto extends BaseDTO implements Serializable {

    private static final long serialVersionUID = -2514768458313413821L;

    /**
     * ID
     */
    private Long id;

    /**
     * 开始时间
     */
    private Timestamp startTime;

    /**
     * 目标网段
     */
    private String ipList;

    /**
     * 执行结果
     */
    private String result;

    /**
     * 结束时间
     */
    private Timestamp endTime;

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
    private boolean cancel;

}
