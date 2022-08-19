package com.cumulus.modules.business.gather.repository;

import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 采集资产数据访问接口
 *
 * @author zhaoff
 */
public interface GatherAssetEsRepository extends ElasticsearchRepository<GatherAssetEs, Long> {

    /**
     * 根据资产ID查找采集资产
     *
     * @param assetId 资产ID
     * @return 采集资产
     */
    GatherAssetEs findByAssetId(Long assetId);

}
