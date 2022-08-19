package com.cumulus.modules.business.other.repository;


import com.cumulus.modules.business.other.entity.es.AbnormalEs;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 风险数据访问接口
 *
 * @author Shijh
 */
public interface AbnormalEsRepository extends ElasticsearchRepository<AbnormalEs, String> {

}
