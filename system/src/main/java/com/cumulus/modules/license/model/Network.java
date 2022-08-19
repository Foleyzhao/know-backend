package com.cumulus.modules.license.model;

import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

/**
 * 系统网络配置模型
 */
@Setter
@XmlRootElement(namespace = "com.cumulus.modules.license.model.SystemInfo")
public class Network implements Serializable {

    private static final long serialVersionUID = -8953750134797073438L;

    /**
     * 网卡名称
     */
    @XmlElement
    private String name;

    /**
     * 网卡状态（up，down）
     */
    @XmlElement
    private String status;

    /**
     * MAC地址
     */
    @XmlElement
    private String mac;

    /**
     * IPV4
     */
    @XmlElement
    private String ipv4;

    /**
     * IPV6
     */
    @XmlElement
    private String ipv6;

    @XmlTransient
    public String getName() {
        return name;
    }

    @XmlTransient
    public String getStatus() {
        return status;
    }

    @XmlTransient
    public String getMac() {
        return mac;
    }

    @XmlTransient
    public String getIpv4() {
        return ipv4;
    }

    @XmlTransient
    public String getIpv6() {
        return ipv6;
    }

}
