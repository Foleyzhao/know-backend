package com.cumulus.modules.business.gather.repository;

import com.cumulus.modules.business.gather.entity.es.GatherItemLogEs;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Date;
import java.util.List;

/**
 * 资产采集项日志数据访问接口
 *
 * @author zhaoff
 */
public interface GatherItemLogEsRepository extends ElasticsearchRepository<GatherItemLogEs, Long> {

    /**
     * 根据采集ID统计已完成的采集项数量
     *
     * @param gatherId 采集ID
     * @return 已完成的采集项数量
     */
    long countAllByGatherId(String gatherId);

    /**
     * 根据采集ID查找采集项日志
     *
     * @param gatherId 采集ID
     * @return 采集项日志列表
     */
    List<GatherItemLogEs> findByGatherId(String gatherId);

    /**
     * 根据采集ID删除采集项日志
     *
     * @param gatherId 采集ID
     */
    void deleteAllByGatherId(String gatherId);

    /**
     * 删除特定时间前的采集项日志
     *
     * @param before 时间
     */
    void deleteByTimeBefore(Date before);

}
