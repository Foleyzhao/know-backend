package com.cumulus.modules.business.gather.repository;

import com.cumulus.modules.business.gather.entity.es.PortEs;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Date;

/**
 * 端口数据访问接口
 *
 * @author Shijh
 */
public interface PortEsRepository extends ElasticsearchRepository<PortEs, String> {

    /**
     * 根据采集资产ID删除端口信息
     *
     * @param assetId 采集资产ID
     */
    void deleteByGatherAssetId(String assetId);

    /**
     * 根据ES采集id获取 分页数据
     *
     * @param gatherAssetId ES 采集Id
     * @param pageable      分页信息
     * @return 返回分页数据
     */
    Page<PortEs> findAllByGatherAssetId(String gatherAssetId, Pageable pageable);

    /**
     * 根据ES采集id获取 分页数据
     *
     * @param gatherAssetId ES 采集Id
     * @param uTime         创建时间
     * @param pageable      分页信息
     * @return 返回分页数据
     */
    Page<PortEs> findAllByGatherAssetIdAndUtime(String gatherAssetId, Date uTime, Pageable pageable);
}
