package com.cumulus.modules.business.entity;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * 远程扫描
 *
 * @author zhangxq
 */
@Setter
@Getter
public class RemoteScan implements Serializable {

    private static final long serialVersionUID = 4463860999862801782L;

    /**
     * ping扫描
     */
    private boolean ping = false;

    /**
     * udp扫描
     */
    private boolean udpScan = false;

    /**
     * udp扫描类型  1全部 2自定义
     */
    private Integer udpRange;

    /**
     * udp端口
     */
    private String udpPorts;

    /**
     * 组件识别 null/0 关  1 弱  2强
     */
    private Integer componentIdentify;

    public RemoteScan() {
        this.setPing(true);
        this.setUdpScan(true);
        this.setUdpRange(1);
        this.setUdpPorts("1-65535");
        this.setComponentIdentify(2);
    }
}
