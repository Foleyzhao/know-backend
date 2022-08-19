package com.cumulus.modules.business.gather.service.gather;

/**
 * 采集数据处理服务接口（ES）
 *
 * @author zhaoff
 */
public interface GatherDataEsService {

    /**
     * 保持或更新资产详情
     *
     * @param gatherId 采集ID
     */
    void saveOrUpdate(String gatherId);

}
