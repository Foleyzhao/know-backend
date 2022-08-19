package com.cumulus.modules.business.gather.repository;

import java.util.Date;
import com.cumulus.modules.business.gather.entity.es.ScanMiddlewareEs;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 远程扫描-中间件数据访问接口
 *
 * @author Shijh
 */
public interface ScanMiddlewareEsRepository extends ElasticsearchRepository<ScanMiddlewareEs, String> {

    /**
     * 根据ES采集id获取 分页数据
     *
     * @param scanAssetId ES 采集Id
     * @param pageable      分页信息
     * @return 返回分页数据
     */
    Page<ScanMiddlewareEs> findAllByScanAssetId(String scanAssetId, PageRequest pageable);

    /**
     * 根据ES采集id获取 分页数据
     *
     * @param scanAssetId ES 采集Id
     * @param uTime         创建时间
     * @param pageable      分页信息
     * @return 返回分页数据
     */
    Page<?> findAllByScanAssetIdAndUtime(String scanAssetId, Date uTime, Pageable pageable);
}
