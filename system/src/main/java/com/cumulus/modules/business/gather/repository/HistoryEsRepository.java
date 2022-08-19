package com.cumulus.modules.business.gather.repository;

import com.cumulus.modules.business.gather.entity.es.HistoryEs;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 历史记录数据访问接口
 *
 * @author Shijh
 */
public interface HistoryEsRepository extends ElasticsearchRepository<HistoryEs, String> {

    /**
     * 根据ES采集id获取 分页数据
     *
     * @param id ES 采集Id
     * @param pageable      分页信息
     * @return 返回分页数据
     */
    Page<HistoryEs> findAllByAssetIdAndSource(Long id, Pageable pageable,String source);
}
