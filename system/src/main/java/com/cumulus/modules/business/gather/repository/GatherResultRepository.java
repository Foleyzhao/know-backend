package com.cumulus.modules.business.gather.repository;

import com.cumulus.modules.business.gather.entity.es.GatherResultEs;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 采集结果数据访问接口
 *
 * @author Shijh
 */
public interface GatherResultRepository extends ElasticsearchRepository<GatherResultEs, Long> {

    /**
     * 查询是否有资产存在
     *
     * @param assetIp 资产ip
     * @return 结果集
     */
    GatherResultEs findByAssetIpAndAndPlanId(String assetIp,Integer planId);
}
