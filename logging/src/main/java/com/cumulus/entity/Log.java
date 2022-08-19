package com.cumulus.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 操作日志实体
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "sys_log")
public class Log implements Serializable {

    private static final long serialVersionUID = -7477469202233974270L;

    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
     * 日志类型
     */
    private String logType;

    /**
     * 请求IP
     */
    private String requestIp;

    /**
     * 请求IP来源
     */
    private String address;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 请求耗时
     */
    private Long time;

    /**
     * 请求异常详情
     */
    private byte[] exceptionDetail;

    /**
     * 创建日期
     */
    @CreationTimestamp
    private Timestamp createTime;

    /**
     * 构造方法
     *
     * @param logType 日志类型
     * @param time    请求耗时
     */
    public Log(String logType, Long time) {
        this.logType = logType;
        this.time = time;
    }

}
