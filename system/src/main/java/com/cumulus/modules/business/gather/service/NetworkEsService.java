package com.cumulus.modules.business.gather.service;

import java.util.List;

import com.cumulus.modules.business.gather.entity.es.NetworkEs;

import org.springframework.data.domain.Pageable;

/**
 * 网络配置接口服务
 *
 * @author Shijh
 */
public interface NetworkEsService {

    /**
     * 批量添加参数
     *
     * @param networkEsList 参数列表
     */
    void saveAll(List<NetworkEs> networkEsList);

    /**
     * 根据id 删除
     *
     * @param id id
     */
    void deleteById(String id);

    /**
     * 根据id进行修改
     *
     * @param networkEs 要修改的对象
     */
    void updateById(NetworkEs networkEs);

    /**
     * 分页查询
     *
     * @param id       资产采集id
     * @param pageable 分页条件
     * @return 结果集
     */
    Object findListRecent(String id, Pageable pageable);

}

