package com.cumulus.modules.business.gather.repository;

import com.cumulus.modules.business.gather.entity.es.GatherAssetLogEs;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * 资产采集日志数据访问接口
 *
 * @author zhaoff
 */
public interface GatherAssetLogEsRepository extends ElasticsearchRepository<GatherAssetLogEs, String> {

    /**
     * 根据资产采集ID查询资产采集日志
     *
     * @param gatherId 资产采集ID
     * @return 资产采集日志
     */
    GatherAssetLogEs findByGatherId(String gatherId);

    /**
     * 根据资产ID查询资产采集日志
     *
     * @param assetId 资产ID
     * @return 资产采集日志列表
     */
    List<GatherAssetLogEs> findByAssetId(Long assetId);

    /**
     * 根据唯一标识查询是同一资产
     *
     * @param flag
     * @return
     */
    List<GatherAssetLogEs> findByFlagIs(Long flag);

}
