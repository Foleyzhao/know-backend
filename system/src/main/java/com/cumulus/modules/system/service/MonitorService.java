package com.cumulus.modules.system.service;

import java.util.Map;

/**
 * 系统监控服务接口
 */
public interface MonitorService {

    /**
     * 获取系统性能相关信息
     *
     * @return 系统性能相关信息
     */
    Map<String, Object> getServers();

}
