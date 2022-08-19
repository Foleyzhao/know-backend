package com.cumulus.modules.license.model;

import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统信息模型
 */
@Setter
@XmlRootElement(namespace = "com.cumulus.modules.license.model.Application")
public class SystemInfo implements Serializable {

    private static final long serialVersionUID = -6608336580383506243L;

    /**
     * 系统网络配置列表
     */
    @XmlElementWrapper(name = "networkList")
    @XmlElement(name = "network")
    private List<Network> networkList = new ArrayList<>();

    /**
     * CPU序列号
     */
    @XmlElement
    private String cpuSerial;

    /**
     * 主板序列号
     */
    @XmlElement
    private String mainBoardSerial;

    @XmlTransient
    public List<Network> getNetworkList() {
        return networkList;
    }

    @XmlTransient
    public String getCpuSerial() {
        return cpuSerial;
    }

    @XmlTransient
    public String getMainBoardSerial() {
        return mainBoardSerial;
    }

}
