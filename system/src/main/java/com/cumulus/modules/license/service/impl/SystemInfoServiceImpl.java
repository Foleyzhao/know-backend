package com.cumulus.modules.license.service.impl;

import com.cumulus.modules.license.model.Network;
import com.cumulus.modules.license.model.SystemInfo;
import com.cumulus.modules.license.service.SystemInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import oshi.hardware.NetworkIF;

/**
 * 系统信息服务实现
 */
@Slf4j
@Service
public class SystemInfoServiceImpl implements SystemInfoService {

    @Override
    public SystemInfo getSystemInfo() {
        try {
            SystemInfo systemInfo = new SystemInfo();
            // CPU序列号
            systemInfo.setCpuSerial(
                    new oshi.SystemInfo().getHardware().getProcessor().getProcessorIdentifier().getProcessorID());
            // 主板序列号
            systemInfo.setMainBoardSerial(
                    new oshi.SystemInfo().getHardware().getComputerSystem().getBaseboard().getSerialNumber());
            for (NetworkIF networkIF : new oshi.SystemInfo().getHardware().getNetworkIFs()) {
                Network network = new Network();
                network.setName(networkIF.getName());
                network.setStatus(networkIF.getIfOperStatus().toString());
                network.setMac(networkIF.getMacaddr());
                network.setIpv4(stringArrayToString(networkIF.getIPv4addr()));
                network.setIpv6(stringArrayToString(networkIF.getIPv6addr()));
                systemInfo.getNetworkList().add(network);
            }
            return systemInfo;
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("get system info error.", e);
            }
            return null;
        }
    }

    /**
     * String数组转字符串
     *
     * @param array String数组
     * @return 字符串
     */
    private String stringArrayToString(String[] array) {
        StringBuilder sb = new StringBuilder();
        if (array != null && array.length > 0) {
            for (int i = 0; i < array.length; i++) {
                if (i < array.length - 1) {
                    sb.append(array[i]).append(",");
                } else {
                    sb.append(array[i]);
                }
            }
        }
        return sb.toString();
    }

}
