package com.cumulus.modules.business.gather.repository;

import java.util.Date;
import com.cumulus.modules.business.gather.entity.es.MiddlewareEs;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 中间件数据访问接口
 *
 * @author Shijh
 */
public interface MiddlewareEsRepository extends ElasticsearchRepository<MiddlewareEs, String> {

    /**
     * 根据ES采集id获取 分页数据
     *
     * @param gatherAssetId ES 采集Id
     * @param pageable      分页信息
     * @return 返回分页数据
     */
    Page<MiddlewareEs> findAllByGatherAssetId(String gatherAssetId, PageRequest pageable);

    /**
     * 根据ES采集id获取 分页数据
     *
     * @param gatherAssetId ES 采集Id
     * @param uTime         创建时间
     * @param pageable      分页信息
     * @return 返回分页数据
     */
    Page<?> findAllByGatherAssetIdAndUtime(String gatherAssetId, Date uTime, Pageable pageable);
}
