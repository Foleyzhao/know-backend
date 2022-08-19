package com.cumulus.modules.business.gather.request;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 连接信息
 *
 * @author zhaoff
 */
@Getter
@Setter
public class ConnectionInfo implements Serializable {

    private static final long serialVersionUID = 3172923126919581762L;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 连接协议（ssh/telnet/winrm/agent）
     */
    private String proto;

    /**
     * 设备编码（缺省为UTF-8）
     */
    private String encoding = "UTF-8";

    /**
     * 资产操作系统类型
     */
    private String sysType;

}
