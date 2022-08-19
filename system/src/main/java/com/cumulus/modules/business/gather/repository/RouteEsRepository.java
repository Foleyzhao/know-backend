package com.cumulus.modules.business.gather.repository;

import com.cumulus.modules.business.gather.entity.es.RouteEs;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Date;

/**
 * 路由数据访问接口
 *
 * @author Shijh
 */
public interface RouteEsRepository extends ElasticsearchRepository<RouteEs, String> {
    /**
     * 根据采集资产Id删除磁盘信息数据
     *
     * @param gatherAssetId 采集资产id
     */
    void deleteByGatherAssetId(String gatherAssetId);

    /**
     * 根据ES采集id获取 分页数据
     *
     * @param gatherAssetId ES 采集Id
     * @param pageable      分页信息
     * @return 返回分页数据
     */
    Page<RouteEs> findAllByGatherAssetId(String gatherAssetId, Pageable pageable);

    /**
     * 根据ES采集id获取 分页数据
     *
     * @param gatherAssetId ES 采集Id
     * @param uTime         创建时间
     * @param pageable      分页信息
     * @return 返回分页数据
     */
    Page<RouteEs> findAllByGatherAssetIdAndUtime(String gatherAssetId, Date uTime, Pageable pageable);
}
