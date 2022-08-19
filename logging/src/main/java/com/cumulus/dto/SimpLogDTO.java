package com.cumulus.dto;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 精简的操作日志传输对象
 */
@Data
public class SimpLogDTO implements Serializable {

    private static final long serialVersionUID = -1385873591009670557L;

    /**
     * ID
     */
    private Long id;

    /**
     * 操作用户用户名
     */
    private String username;

    /**
     * 描述
     */
    private String description;

    /**
     * 请求IP
     */
    private String requestIp;

    /**
     * 请求耗时
     */
    private Long time;

    /**
     * 日志类型
     */
    private String logType;

    /**
     * 创建日期
     */
    private Timestamp createTime;

}
