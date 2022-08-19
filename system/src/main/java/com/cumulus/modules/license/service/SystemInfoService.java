package com.cumulus.modules.license.service;

import com.cumulus.modules.license.model.SystemInfo;

/**
 * 系统信息服务接口
 */
public interface SystemInfoService {

    /**
     * 获取系统信息
     *
     * @return 系统信息
     */
    SystemInfo getSystemInfo();

}
