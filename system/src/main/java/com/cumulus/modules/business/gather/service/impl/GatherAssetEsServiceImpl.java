package com.cumulus.modules.business.gather.service.impl;

import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.repository.GatherAssetEsRepository;
import com.cumulus.modules.business.gather.service.GatherAssetEsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 采集资产服务实现
 *
 * @author zhaoff
 */
@Service
public class GatherAssetEsServiceImpl implements GatherAssetEsService {

    /**
     * 采集资产数据访问接口
     */
    @Autowired
    private GatherAssetEsRepository gatherAssetRepository;

    @Override
    public void save(GatherAssetEs gatherAsset) {
        gatherAssetRepository.save(gatherAsset);
    }

    @Override
    public GatherAssetEs findGatherAssetByAssetId(Long assetId) {
        return gatherAssetRepository.findByAssetId(assetId);
    }

}
