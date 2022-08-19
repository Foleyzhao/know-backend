package com.cumulus.dto;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * ERROR类型的操作日志传输对象
 */
@Data
public class ErrorLogDTO implements Serializable {

    private static final long serialVersionUID = -1995715864675560903L;

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
     * 请求方法名
     */
    private String method;

    /**
     * 请求参数
     */
    private String params;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 请求IP
     */
    private String requestIp;

    /**
     * 请求IP来源
     */
    private String address;

    /**
     * 创建日期
     */
    private Timestamp createTime;

}
