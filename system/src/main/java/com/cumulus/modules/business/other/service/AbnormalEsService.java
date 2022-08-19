package com.cumulus.modules.business.other.service;

import java.util.List;


import com.cumulus.modules.business.other.entity.es.AbnormalEs;

import org.springframework.data.domain.Pageable;

/**
 * 异常服务接口
 *
 * @author Shijh
 */
public interface AbnormalEsService {

    /**
     * 批量添加参数
     *
     * @param abnormalEs 参数列表
     */
    void saveAll(List<AbnormalEs> abnormalEs);

    /**
     * 根据id 删除
     *
     * @param id id
     */
    void deleteById(String id);

    /**
     * 根据id进行修改
     *
     * @param abnormalEs 要修改的对象
     */
    void updateById(AbnormalEs abnormalEs);

    /**
     * 分页查询
     *
     * @param id       资产表的id
     * @param pageable 分页条件
     * @return
     */
    Object findList(String id, Pageable pageable);

    /**
     * 获取异常详情
     *
     * @param ip 资产ip
     *
     * @return 结果集
     */
    Object getAbnormal(String ip,Pageable pageable);

}
