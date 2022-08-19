package com.cumulus.modules.license.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 授权文件实体
 */
@Getter
@Setter
@AllArgsConstructor
public final class License implements Serializable {

    private static final long serialVersionUID = -7240431232413335705L;

    /**
     * 分隔符
     */
    private final byte[] splitFlag = new byte[]{(byte) ((int) (System.nanoTime() & 127L)),
            (byte) ((int) (System.nanoTime() & 127L))};

    /**
     * 申请时间
     */
    private final long applyTime = System.currentTimeMillis();

    /**
     * 过期时间
     */
    private final long expireTime;

    /**
     * 可被允许的IP地址
     */
    private List<String> ipAddress;

    /**
     * 可被允许的MAC地址
     */
    private List<String> macAddress;

    /**
     * 可被允许的CPU序列号
     */
    private String cpuSerial;

    /**
     * 可被允许的主板序列号
     */
    private String mainBoardSerial;
}
