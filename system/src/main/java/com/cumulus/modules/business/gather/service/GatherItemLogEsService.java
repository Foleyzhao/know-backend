package com.cumulus.modules.business.gather.service;

/**
 * 资产采集项的采集日志服务接口
 *
 * @author zhaoff
 */
public interface GatherItemLogEsService {

    /**
     * 根据采集ID删除采集项日志
     *
     * @param gatherId 采集ID
     */
    void deleteItemLogByGatherId(String gatherId);

    /**
     * 删除一天以前的采集项日志
     */
    void deleteItemLogOneDayAgo();

}
