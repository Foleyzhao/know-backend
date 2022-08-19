package com.cumulus.modules.business.gather.service;

import java.util.List;

import com.cumulus.modules.business.gather.entity.es.ServiceEs;

import org.springframework.data.domain.Pageable;

/**
 * 服务-服务接口
 *
 * @author Shijh
 */
public interface ServiceEsService {

    /**
     * 批量添加参数
     *
     * @param serves 参数列表
     */
    void saveAll(List<ServiceEs> serves);

    /**
     * 根据id 删除
     *
     * @param id id
     */
    void deleteById(String id);

    /**
     * 根据id进行修改
     *
     * @param serve 要修改的对象
     */
    void updateById(ServiceEs serve);


    /**
     * 分页查询
     *
     * @param id       资产采集id
     * @param pageable 分页条件
     * @return 结果集
     */
    Object findListRecent(String id, Pageable pageable);

}
