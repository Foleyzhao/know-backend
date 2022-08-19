package com.cumulus.modules.business.gather.controller;

import java.util.Optional;
import com.cumulus.modules.business.entity.Asset;
import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.entity.es.ScanBasicInfoEs;
import com.cumulus.modules.business.gather.service.ScanBasicInfoEsService;
import com.cumulus.modules.business.repository.AssetRepository;
import com.cumulus.modules.business.service.AssetService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 远程扫描-基础信息控制层
 *
 * @author Shijh
 */
@RestController
@RequestMapping("/api/es/scan-basic-info/")
public class ScanBasicInfoEsController {

    /**
     * 远程扫描-基本信息接口服务
     */
    @Autowired
    private ScanBasicInfoEsService scanBasicInfoEsService;

    /**
     * 资产数据访问接口
     */
    @Autowired
    private AssetRepository assetRepository;

    /**
     * 资产服务接口
     */
    @Autowired
    private AssetService assetService;

}
