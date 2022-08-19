package com.cumulus.modules.business.gather.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.cumulus.modules.business.entity.Asset;
import com.cumulus.modules.business.entity.AssetTag;
import com.cumulus.modules.business.gather.common.constant.GatherConstants;
import com.cumulus.modules.business.gather.common.utils.BusinessCommon;
import com.cumulus.modules.business.gather.common.utils.CommUtils;
import com.cumulus.modules.business.gather.entity.es.DBEs;
import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.entity.es.MiddlewareEs;
import com.cumulus.modules.business.gather.entity.es.ServiceEs;
import com.cumulus.modules.business.gather.repository.DBEsRepository;
import com.cumulus.modules.business.gather.repository.MiddlewareEsRepository;
import com.cumulus.modules.business.gather.repository.ServiceEsRepository;
import com.cumulus.modules.business.gather.request.GatherItemLog;
import com.cumulus.modules.business.repository.AssetRepository;
import com.cumulus.modules.business.repository.AssetTagRepository;

import com.cumulus.modules.business.service.AssetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 服务处理类
 *
 * @author : shenjc
 */
@Slf4j
@Component
public class ServiceHandler extends ItemLogHandler {

    /**
     * 服务数据访问接口
     */
    @Autowired
    private ServiceEsRepository repository;

    /**
     * 数据库数据访问接口
     */
    @Autowired
    private DBEsRepository dbEsRepository;

    /**
     * 中间件数据访问接口
     */
    @Autowired
    private MiddlewareEsRepository middlewareEsRepository;

    /**
     * 资产标签数据访问接口
     */
    @Autowired
    private AssetTagRepository assetTagRepository;

    /**
     * 资产服务类
     */
    @Autowired
    private AssetService assetService;

    public ServiceHandler() {
        this.esIndex = GatherConstants.ES_INDEX_SERVICE;
    }

    @Override
    public List<String> supportAssetTypes() {
        return Collections.singletonList(GatherConstants.TYPE_HOST.toString());
    }

    @Override
    public void handle(GatherAssetEs asset, Set<GatherItemLog> itemLogs) throws Exception {
        if (null == asset || CommUtils.isEmptyOfCollection(itemLogs)) {
            return;
        }
        List<ServiceEs> services = new ArrayList<>();
        for (GatherItemLog itemLog : itemLogs) {
            if (null == itemLog) {
                continue;
            }
            String itemName = BusinessCommon.getNameFromItemKey(itemLog.getItemKey());
            Map<String, Object> elite = itemLog.getElite();
            if (null == elite || !elite.containsKey(itemName)) {
                continue;
            }
            List<Map<String, Object>> serviceList = (List<Map<String, Object>>) elite.get(itemName);
            if (CommUtils.isEmptyOfCollection(serviceList)) {
                continue;
            }
            for (Map<String, Object> data : serviceList) {
                if (data.isEmpty()) {
                    continue;
                }
                ServiceEs service = getService(data, asset);
                services.add(service);
            }
        }
        putDetails(asset, repository, services);
    }

    @Override
    public void deleteHandle(GatherAssetEs asset) {
        repository.deleteByGatherAssetId(asset.getId());
    }

    /**
     * 生成服务信息
     *
     * @param data  采集数据
     * @param asset 资产
     * @return 软件
     */
    private ServiceEs getService(Map<String, Object> data, GatherAssetEs asset) {
        ServiceEs service = new ServiceEs();
        service.setId(CommUtils.createAuditId());
        service.setAssetId(asset.getAssetId());
        service.setGatherAssetId(asset.getId());
        service.setDetail(data);
        service.setUtime(asset.getUtime());
        service.setName((String) data.get("name"));
        service.setTurnOn((String) data.get("auto"));
        service.setState((String) data.get("state"));
        //中间件
        AssetTag assetTagsMiddleware = assetTagRepository.findByParentIs(AssetTag.ASSET_TAG_MIDDLEWARE,service.getName().trim());
        if (null != assetTagsMiddleware) {
            DBEs dbEs = new DBEs();
            dbEs.setId(CommUtils.createAuditId());
            dbEs.setGatherAssetId(asset.getId());
            dbEs.setAssetId(asset.getAssetId());
            dbEs.setUtime(asset.getUtime());
            dbEsRepository.save(dbEs);
        }
        // 数据库
        AssetTag assetTagsDb = assetTagRepository.findByParentIs(AssetTag.ASSET_TAG_DB,service.getName().trim());
        if (null != assetTagsDb) {
            MiddlewareEs middlewareEs = new MiddlewareEs();
            middlewareEs.setId(CommUtils.createAuditId());
            middlewareEs.setGatherAssetId(asset.getId());
            middlewareEs.setAssetId(asset.getAssetId());
            middlewareEs.setUtime(asset.getUtime());
            middlewareEs.setName(service.getName());
            middlewareEsRepository.save(middlewareEs);
        }
        assetService.updateAssetTag(asset.getAssetId(), assetTagsMiddleware, assetTagsDb);
        return service;
    }
}
