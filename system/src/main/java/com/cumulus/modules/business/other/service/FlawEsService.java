package com.cumulus.modules.business.other.service;

import java.util.List;

import com.cumulus.modules.business.other.entity.es.FlawEs;

import org.springframework.data.domain.Pageable;

/**
 * 漏洞服务接口
 *
 * @author Shijh
 */
public interface FlawEsService {


    /**
     * 批量添加参数
     *
     * @param flawEs 参数列表
     */
    void saveAll(List<FlawEs> flawEs);

    /**
     * 分页查询
     *
     * @param pageable 分页对象
     * @return 结果集
     */
    Object finAll(Pageable pageable);

    /**
     * 根据id 删除
     *
     * @param id id
     */
    void deleteById(String id);

    /**
     * 根据id进行修改
     *
     * @param flawEs 要修改的对象
     */
    void updateById(FlawEs flawEs);

    /**
     * 根据资产 ip 查询风险详情
     *
     * @param ip       资产ip
     * @param pageable 分页条件
     * @return 结果集
     */
    Object getFlawInformation(String ip, Pageable pageable);

}
