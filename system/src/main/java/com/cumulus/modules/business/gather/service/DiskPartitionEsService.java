package com.cumulus.modules.business.gather.service;

import java.util.List;

import com.cumulus.modules.business.gather.entity.es.DiskPartitionEs;

import org.springframework.data.domain.Pageable;

/**
 * 磁盘信息服务接口
 *
 * @author shijh
 */
public interface DiskPartitionEsService {

    /**
     * 批量添加参数
     *
     * @param diskPartitionEs 参数列表
     */
    void saveAll(List<DiskPartitionEs> diskPartitionEs);

    /**
     * 根据id 删除
     *
     * @param id id
     */
    void deleteById(String id);

    /**
     * 根据id进行修改
     *
     * @param diskPartitionEs 要修改的对象
     */
    void updateById(DiskPartitionEs diskPartitionEs);

    /**
     * 分页查询
     *
     * @param id       资产表的id
     * @param pageable 分页条件
     * @return
     */
    Object findListRecent(String id, Pageable pageable);

}
