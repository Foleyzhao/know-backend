package com.cumulus.modules.business.gather.service;

import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;

/**
 * 采集资产服务接口
 *
 * @author zhaoff
 */
public interface GatherAssetEsService {

    /**
     * 保存采集资产
     *
     * @param gatherAsset 采集资产
     */
    void save(GatherAssetEs gatherAsset);


    /**
     * 根据资产ID获取采集资产
     *
     * @param assetId 资产ID
     * @return 采集资产
     */
    GatherAssetEs findGatherAssetByAssetId(Long assetId);

}
