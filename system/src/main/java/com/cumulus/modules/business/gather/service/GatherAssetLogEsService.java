package com.cumulus.modules.business.gather.service;

import com.cumulus.modules.business.gather.entity.es.GatherAssetLogEs;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 资产采集日志服务接口
 *
 * @author zhaoff
 */
public interface GatherAssetLogEsService {

    /**
     * 统计任务的执行结果
     * {
     *     0:xx //成功
     *     1:xx //失败
     *     2:xx //部分成功
     * }
     *
     * @param taskLogId 任务日志ID
     * @return 结果
     */
    Map<Integer, Long> taskResultStatistics(String taskLogId);

    /**
     * 获取状态为未开始的资产采集日志
     *
     * @param planIds 正在运行的采集计划ID
     * @param size    采集日志数量
     * @return 未采集的资产采集日志
     */
    List<GatherAssetLogEs> getUnGatherTask(Collection<Long> planIds, int size);

    /**
     * 根据采集任务ID和是否使用agent标记查询正在运行采集日志的采集ID
     *
     * @param planId    采集任务ID
     * @param userAgent 是否使用agent采集
     * @return 采集ID列表
     */
    List<String> getRunningTasksGatherId(Long planId, Boolean userAgent);

    /**
     * 根据采集任务ID和是否使用agent标记查询所有未完成采集日志的采集ID
     * 状态包含未开始、排队中、正在执行
     *
     * @param planId    采集任务ID
     * @param userAgent 是否使用agent采集
     * @return 采集ID列表
     */
    List<String> getUnCompletedGatherId(Long planId, Boolean userAgent);

    /**
     * 保存采集资产日志
     *
     * @param gatherAssetLogs 采集资产日志列表
     */
    void saveAll(List<GatherAssetLogEs> gatherAssetLogs);

}
