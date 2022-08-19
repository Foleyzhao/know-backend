package com.cumulus.modules.license.model;

import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

/**
 * 申请授权文件模型
 */
@Setter
@XmlRootElement
public class Application implements Serializable {

    private static final long serialVersionUID = 8062053350844994505L;

    /**
     * 产品名称
     */
    @XmlElement
    private String productName;

    /**
     * 申请时间
     */
    @XmlElement
    private long applyTime = System.currentTimeMillis();

    /**
     * 系统信息
     */
    @XmlElement(name = "systemInfo")
    private SystemInfo systemInfo;

    @XmlTransient
    public String getProductName() {
        return productName;
    }

    @XmlTransient
    public long getApplyTime() {
        return applyTime;
    }

    @XmlTransient
    public SystemInfo getSystemInfo() {
        return systemInfo;
    }

}
