package com.cumulus.modules.business.gather.service;

import org.springframework.data.domain.Pageable;

/**
 * 远程扫描-数据库接口服务
 *
 * @author Shijh
 */
public interface ScanDBEsService {

    /**
     * 分页查询 最近的记录
     *
     * @param id scanAssetId
     * @param pageable 分页条件
     * @return
     */
    Object findListRecent(String id, Pageable pageable);
}
