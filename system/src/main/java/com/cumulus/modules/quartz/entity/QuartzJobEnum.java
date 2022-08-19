package com.cumulus.modules.quartz.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotBlank;

/**
 * 定时任务枚举
 *
 * @author : shenjc
 */
@Getter
@AllArgsConstructor
public enum QuartzJobEnum {

    /**
     * 定时任务枚举
     */
    VUL_SCAN_RETEST("vulScanRetest", "vulnerabilityHistoryEsServiceImpl", ""),
    LOG_FILE_ARCHIVE("archive", "logFileServiceImpl", "saveLogFileAuto"),
    ASSET_DETECT("detect", "detectTaskServiceImpl", "doDetectTask"),
    ASSET_GATHER("gather", "", ""),
    VULNERABILITY("vulnerability", "scanTaskManager", "runNow"),
    SAVE_VULNERABILITY_COUNT("countVulnerability", "countVulnerabilityServiceImpl", "findCountVul");

    /**
     * 任务类型
     */
    private final String jobType;

    /**
     * Bean名称
     */
    @NotBlank
    private final String beanName;

    /**
     * 方法名称
     */
    @NotBlank
    private final String methodName;
}
