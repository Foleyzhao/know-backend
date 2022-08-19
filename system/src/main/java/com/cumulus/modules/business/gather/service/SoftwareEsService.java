package com.cumulus.modules.business.gather.service;

import java.util.List;

import com.cumulus.modules.business.gather.entity.es.SoftwareEs;

import org.springframework.data.domain.Pageable;

/**
 * 已转软件接口服务
 *
 * @author Shijh
 */
public interface SoftwareEsService {

    /**
     * 批量添加参数
     *
     * @param softwareList 参数列表
     */
    void saveAll(List<SoftwareEs> softwareList);

    /**
     * 根据id 删除
     *
     * @param id id
     */
    void deleteById(String id);

    /**
     * 根据id进行修改
     *
     * @param software 要修改的对象
     */
    void updateById(SoftwareEs software);

    /**
     * 分页查询
     *
     * @param id       资产表的id
     * @param pageable 分页条件
     * @return 返回分页结果
     */
    Object findListRecent(String id, Pageable pageable);

}

