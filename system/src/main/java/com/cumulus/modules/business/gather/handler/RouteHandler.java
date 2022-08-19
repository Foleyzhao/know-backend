package com.cumulus.modules.business.gather.handler;

import com.cumulus.modules.business.gather.common.constant.GatherConstants;
import com.cumulus.modules.business.gather.common.utils.BusinessCommon;
import com.cumulus.modules.business.gather.common.utils.CommUtils;
import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.entity.es.RouteEs;
import com.cumulus.modules.business.gather.repository.RouteEsRepository;
import com.cumulus.modules.business.gather.request.GatherItemLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author : shenjc
 */
@Slf4j
@Component
public class RouteHandler extends ItemLogHandler {

    @Autowired
    private RouteEsRepository repository;

    RouteHandler() {
        this.esIndex = GatherConstants.ES_INDEX_ROUTE;
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
        List<RouteEs> routes = new ArrayList<>();
        for (GatherItemLog itemLog : itemLogs) {
            if (null == itemLog) {
                continue;
            }
            String itemName = BusinessCommon.getNameFromItemKey(itemLog.getItemKey());
            Map<String, Object> elite = itemLog.getElite();
            if (null == elite || !elite.containsKey(itemName)) {
                continue;
            }
            List<Map<String, Object>> routeList = (List<Map<String, Object>>) elite.get(itemName);
            if (CommUtils.isEmptyOfCollection(routeList)) {
                continue;
            }
            for (Map<String, Object> data : routeList) {
                if (data.isEmpty()) {
                    continue;
                }
                RouteEs route = getRoute(data, asset);
                routes.add(route);
            }
        }
        putDetails(asset, repository, routes);
    }

    @Override
    public void deleteHandle(GatherAssetEs asset) {
        repository.deleteByGatherAssetId(asset.getId());
    }

    /**
     * 生成路由信息
     *
     * @param data  采集数据
     * @param asset 资产
     * @return 路由
     */
    private RouteEs getRoute(Map<String, Object> data, GatherAssetEs asset) {
        RouteEs route = new RouteEs();
        route.setId(CommUtils.createAuditId());
        route.setAssetId(asset.getAssetId());
        route.setGatherAssetId(asset.getId());
        route.setDetail(data);
        route.setTarget((String) data.get("targetAdd"));
        route.setIfs((String) data.get("outInterface"));
        route.setGateway((String) data.get("gatewayAdd"));
        route.setMask((String) data.get("maskAdd"));
        route.setUtime(asset.getUtime());
        return route;
    }
}
