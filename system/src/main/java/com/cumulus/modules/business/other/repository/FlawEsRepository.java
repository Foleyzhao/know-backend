package com.cumulus.modules.business.other.repository;


import com.cumulus.modules.business.other.entity.es.FlawEs;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 漏洞数据访问接口
 *
 * @author Shijh
 */
public interface FlawEsRepository extends ElasticsearchRepository<FlawEs, String> {
    /**
     * 根据资产ip查询并按照采集时间排序
     *
     * @param ip ip
     * @return 结果
     */
    FlawEs findAllByIpOrderByUtimeDesc(String ip);

}
