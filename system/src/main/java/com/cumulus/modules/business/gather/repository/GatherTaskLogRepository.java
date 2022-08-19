package com.cumulus.modules.business.gather.repository;

import com.cumulus.modules.business.gather.entity.es.GatherTaskLogEs;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * 采集任务日志数据访问接口
 *
 * @author zhaoff
 */
public interface GatherTaskLogRepository extends ElasticsearchRepository<GatherTaskLogEs, String> {

    /**
     * 根据任务状态查询采集任务日志
     *
     * @param states 任务状态
     * @return 采集任务日志
     */
    List<GatherTaskLogEs> findByTaskStateIn(List<Integer> states);

}
