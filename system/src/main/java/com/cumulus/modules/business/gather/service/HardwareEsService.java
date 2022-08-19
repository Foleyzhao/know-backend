package com.cumulus.modules.business.gather.service;

import java.util.List;

import com.cumulus.modules.business.gather.entity.es.HardwareEs;

import org.springframework.data.domain.Pageable;

/**
 * 中硬件信息服务接口
 *
 * @author shijh
 */
public interface HardwareEsService {

    /**
     * 批量添加参数
     *
     * @param hardwareEs 参数列表
     */
    void saveAll(List<HardwareEs> hardwareEs);

    /**
     * 根据id 删除
     *
     * @param id id
     */
    void deleteById(String id);

    /**
     * 根据id进行修改
     *
     * @param hardwareEs 要修改的对象
     */
    void updateById(HardwareEs hardwareEs);

    /**
     * 分页查询
     *
     * @param id       资产采集id
     * @param pageable 分页条件
     * @return 结果集
     */
    Object findListRecent(String id, Pageable pageable);


}
