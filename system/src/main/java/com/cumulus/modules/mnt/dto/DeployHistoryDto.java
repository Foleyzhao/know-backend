package com.cumulus.modules.mnt.dto;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 部署历史传输对象
 */
@Data
public class DeployHistoryDto implements Serializable {

    private static final long serialVersionUID = 2611036171230243404L;

    /**
     * ID
     */
    private String id;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 服务器IP
     */
    private String ip;

    /**
     * 部署时间
     */
    private Timestamp deployDate;

    /**
     * 部署人员
     */
    private String deployUser;

    /**
     * 部署编号
     */
    private Long deployId;

}
