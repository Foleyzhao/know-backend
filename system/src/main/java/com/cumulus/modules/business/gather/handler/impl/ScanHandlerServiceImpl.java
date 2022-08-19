package com.cumulus.modules.business.gather.handler.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import com.alibaba.fastjson.JSONObject;
import com.cumulus.modules.business.detect.dto.DetectReponse;
import com.cumulus.modules.business.entity.Asset;
import com.cumulus.modules.business.entity.AssetExtend;
import com.cumulus.modules.business.entity.AssetTag;
import com.cumulus.modules.business.gather.common.utils.CommUtils;
import com.cumulus.modules.business.gather.entity.es.ScanBasicInfoEs;
import com.cumulus.modules.business.gather.entity.es.ScanDBEs;
import com.cumulus.modules.business.gather.entity.es.ScanMiddlewareEs;
import com.cumulus.modules.business.gather.entity.es.ScanServicePortEs;
import com.cumulus.modules.business.gather.handler.ScanHandlerService;
import com.cumulus.modules.business.gather.repository.ScanBasicInfoEsRepository;
import com.cumulus.modules.business.gather.repository.ScanDBEsRepository;
import com.cumulus.modules.business.gather.repository.ScanMiddlewareEsRepository;
import com.cumulus.modules.business.gather.repository.ScanServicePortEeRepository;
import com.cumulus.modules.business.repository.AssetRepository;
import com.cumulus.modules.business.repository.AssetTagRepository;

import com.cumulus.modules.business.service.AssetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 远程扫描-解析
 *
 * @author shijh
 */
@Slf4j
@Service
public class ScanHandlerServiceImpl implements ScanHandlerService {

    /**
     * 资产数据访问接口
     */
    @Autowired
    private AssetRepository assetRepository;

    /**
     * 远程扫描-数据访问接口
     */
    @Autowired
    private ScanBasicInfoEsRepository scanBasicInfoEsRepository;

    /**
     * 远程扫描-服务数据访问接口
     */
    @Autowired
    private ScanServicePortEeRepository scanServicePortEeRepository;

    /**
     * 资产标签数据访问接口
     */
    @Autowired
    private AssetTagRepository assetTagRepository;

    /**
     * 远程扫描-中间件数据访问接口
     */
    @Autowired
    private ScanMiddlewareEsRepository scanMiddlewareEsRepository;

    /**
     * 资产服务类
     */
    @Autowired
    private AssetService assetService;

    /**
     * 远程扫描-数据库数据访问接口
     */
    @Autowired
    private ScanDBEsRepository scanDBEsRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handle(Map<String, Object> take) {
        log.info("map received : " + JSONObject.toJSONString(take));
        if ("web".equals(take.get("type"))) {
            return;
        }
        DetectReponse.DetectReponseIp detectReponseIp = null;
        if ("ip".equals(take.get("type"))) {
            detectReponseIp = JSONObject.parseObject(JSONObject.toJSONString(take), DetectReponse.DetectReponseIp.class);
        }
        DetectReponse detectReponse = JSONObject.parseObject(JSONObject.toJSONString(take), DetectReponse.class);
        Asset asset = assetRepository.findById(Long.parseLong(detectReponse.getId())).get();
        //基本信息
        ScanBasicInfoEs scanBasicInfoEs = new ScanBasicInfoEs();
        scanBasicInfoEs.setId(CommUtils.createAuditId());
        scanBasicInfoEs.setAssetId(asset.getId());
        scanBasicInfoEs.setDetail(take);
        scanBasicInfoEs.setOperatingSystem(detectReponseIp.getOs());
        scanBasicInfoEs.setLocation(detectReponse.getGeo());
        scanBasicInfoEs.setScanAssetId(asset.getScanAssetId());
        scanBasicInfoEs.setUtime(new Date());
        scanBasicInfoEsRepository.save(scanBasicInfoEs);
        assetRepository.updateScanAssetIdById(asset.getId(), scanBasicInfoEs.getId());

        //服务与端口
        List<DetectReponse.Port> ports = detectReponseIp.getPorts();
        for (DetectReponse.Port port : ports) {
            ScanServicePortEs scanServicePortEs = new ScanServicePortEs();
            scanServicePortEs.setId(CommUtils.createAuditId());
            scanServicePortEs.setAssetId(asset.getId());
            scanServicePortEs.setScanAssetId(scanBasicInfoEs.getId());
            scanServicePortEs.setProductName(port.getProduct());
            scanServicePortEs.setVersion(port.getVersion());
            scanServicePortEs.setState(port.getState());
            scanServicePortEs.setPort(port.getPort());
            scanServicePortEs.setProtocol(port.getType());
            scanServicePortEs.setUtime(scanBasicInfoEs.getUtime());
            scanServicePortEeRepository.save(scanServicePortEs);

            //这里去做对比的是产品名称
            //中间件
            AssetTag assetTagsMiddleware = assetTagRepository.findByParentIs(AssetTag.ASSET_TAG_MIDDLEWARE, port.getProduct().trim());
            if (null != assetTagsMiddleware) {
                ScanMiddlewareEs scanMiddlewareEs = new ScanMiddlewareEs();
                scanMiddlewareEs.setId(CommUtils.createAuditId());
                scanMiddlewareEs.setAssetId(asset.getId());
                scanMiddlewareEs.setScanAssetId(scanBasicInfoEs.getId());
                scanMiddlewareEs.setName(port.getProduct());
                scanMiddlewareEs.setPort(port.getPort());
                scanMiddlewareEs.setUtime(scanBasicInfoEs.getUtime());
                scanMiddlewareEsRepository.save(scanMiddlewareEs);
            }
            //数据库
            AssetTag assetTagDb = assetTagRepository.findByParentIs(AssetTag.ASSET_TAG_DB, port.getProduct().trim());
            if (null != assetTagDb) {
                //数据库
                ScanDBEs dbEs = new ScanDBEs();
                dbEs.setId(CommUtils.createAuditId());
                dbEs.setAssetId(asset.getId());
                dbEs.setScanAssetId(scanBasicInfoEs.getId());
                dbEs.setPort(port.getPort());
                dbEs.setName(port.getProduct());
                dbEs.setUtime(scanBasicInfoEs.getUtime());
                scanDBEsRepository.save(dbEs);
            }
            assetService.updateAssetTag(asset.getId(), assetTagsMiddleware, assetTagDb);
            Asset assetPort = assetRepository.queryAssetByCompleteIpEqualsAndPortEqualsAndAssetCategoryEquals(asset.getCompleteIp(), asset.getPort(), Asset.CATEGORY_PORT);
            AssetExtend assetExtend = null;
            if (null == assetPort.getAssetExtend()) {
                assetExtend = new AssetExtend();
            } else {
                assetExtend = assetPort.getAssetExtend();
            }
            assetExtend.setName(port.getName());
            assetExtend.setProduct(port.getProduct());
            assetExtend.setVersion(port.getVersion());
            assetExtend.setExtrainfo(port.getExtrainfo());
            assetExtend.setReason(port.getReason());
            assetExtend.setType(port.getType());
            assetExtend.setState(port.getState());
            assetExtend.setConf(port.getConf());
            assetExtend.setCpe(port.getCpe());
            assetPort.setAssetExtend(assetExtend);
            assetRepository.save(assetPort);
        }
    }
}
