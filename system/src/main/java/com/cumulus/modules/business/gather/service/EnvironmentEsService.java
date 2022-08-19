package com.cumulus.modules.business.gather.service;

import java.util.List;

import com.cumulus.modules.business.gather.entity.es.EnvironmentEs;

import org.springframework.data.domain.Pageable;

/**
 * 环境变量服务接口
 *
 * @author Shijh
 */
public interface EnvironmentEsService {

    /**
     * 批量添加参数
     *
     * @param environmentEs 参数列表
     */
    void saveAll(List<EnvironmentEs> environmentEs);

    /**
     * 根据id 删除
     *
     * @param id id
     */
    void deleteById(String id);

    /**
     * 根据id进行修改
     *
     * @param environmentEs 要修改的对象
     */
    void updateById(EnvironmentEs environmentEs);

    /**
     * 分页查询
     *
     * @param id 扫描资产id
     * @param pageable 分页条件
     * @return 结果集
     */
    Object findListRecent(String id, Pageable pageable);

}
