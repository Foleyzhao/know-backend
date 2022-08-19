package com.cumulus.modules.business.gather.service;

import org.springframework.data.domain.Pageable;

/**
 * 远程扫描-端口与服务接口服务
 *
 * @author Shijh
 */
public interface ScanServicePortEsService {

    /**
     * 分页查询
     *
     * @param scanAssetId 资产表的scanAssetId
     * @param pageable    分页条件
     * @return 结果集
     */
    Object findListRecent(String scanAssetId, Pageable pageable);
}
