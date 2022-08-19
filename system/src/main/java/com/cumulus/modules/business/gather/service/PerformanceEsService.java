package com.cumulus.modules.business.gather.service;

import java.util.List;

import com.cumulus.modules.business.gather.entity.es.PerformanceEs;

import org.springframework.data.domain.Pageable;

/**
 * 性能监控服务接口
 *
 * @author Shijh
 */
public interface PerformanceEsService {

    /**
     * 批量添加参数
     *
     * @param performanceEsList 参数列表
     */
    void saveAll(List<PerformanceEs> performanceEsList);

    /**
     * 根据id 删除
     *
     * @param id id
     */
    void deleteById(String id);

    /**
     * 根据id进行修改
     *
     * @param performanceEs 要修改的对象
     */
    void updateById(PerformanceEs performanceEs);

    /**
     * 分页查询
     *
     * @param id       资产采集id
     * @param pageable 分页条件
     * @return 结果集
     */
    Object findList(String id, Pageable pageable);

}
