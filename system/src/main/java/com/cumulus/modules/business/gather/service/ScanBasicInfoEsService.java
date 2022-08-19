package com.cumulus.modules.business.gather.service;

import org.springframework.data.domain.Pageable;

/**
 * 远程扫描-基本信息接口服务
 *
 * @author Shijh
 */
public interface ScanBasicInfoEsService {

    /**
     * 分页查询 最近的记录
     *
     * @param id       资产表的id
     * @param pageable 分页条件
     * @return 返回分页结果
     */
    Object findListRecent(String id, Pageable pageable);
}
