package com.cumulus.modules.business.gather.service;

import java.util.List;

import com.cumulus.modules.business.gather.entity.es.HistoryEs;

import org.springframework.data.domain.Pageable;

/**
 * 历史变更服务接口
 *
 * @author Shijh
 */
public interface HistoryEsService {

    /**
     * 批量添加参数
     *
     * @param historyEsList 参数列表
     */
    void saveAll(List<HistoryEs> historyEsList);

    /**
     * 根据id 删除
     *
     * @param id id
     */
    void deleteById(String id);

    /**
     * 根据id进行修改
     *
     * @param historyEs 要修改的对象
     */
    void updateById(HistoryEs historyEs);

    /**
     * 分页查询
     *
     * @param id       资产采集id
     * @param pageable 分页条件
     * @return 结果集
     */
    Object findListRecent(Long id, Pageable pageable);
}
