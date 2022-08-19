package com.cumulus.modules.business.gather.handler;

import com.cumulus.modules.business.gather.common.constant.GatherConstants;
import com.cumulus.modules.business.gather.common.utils.BusinessCommon;
import com.cumulus.modules.business.gather.common.utils.CommUtils;
import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.entity.es.NetworkEs;
import com.cumulus.modules.business.gather.repository.NetworkEsRepository;
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
 * 网络处理类
 *
 * @author : shenjc
 */
@Slf4j
@Component
public class NetWorkHandler extends ItemLogHandler {
    @Autowired
    private NetworkEsRepository repository;

    public NetWorkHandler() {
        this.esIndex = GatherConstants.ES_INDEX_NET_CONFIG;
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
        List<NetworkEs> networks = new ArrayList<>();
        for (GatherItemLog itemLog : itemLogs) {
            if (null == itemLog) {
                continue;
            }
            String itemName = BusinessCommon.getNameFromItemKey(itemLog.getItemKey());
            Map<String, Object> elite = itemLog.getElite();
            if (null == elite || !elite.containsKey(itemName)) {
                continue;
            }
            List<Map<String, Object>> networkList = (List<Map<String, Object>>) elite.get(itemName);
            if (CommUtils.isEmptyOfCollection(networkList)) {
                continue;
            }
            for (Map<String, Object> data : networkList) {
                if (data.isEmpty()) {
                    continue;
                }
                NetworkEs network = getNetwork(data, asset);
                networks.add(network);
            }
        }
        putDetails(asset, repository, networks);
    }

    @Override
    public void deleteHandle(GatherAssetEs asset) {
        repository.deleteByGatherAssetId(asset.getId());
    }

    /**
     * 生成网络信息
     *
     * @param data  采集数据
     * @param asset 资产
     * @return 网络信息
     */
    private NetworkEs getNetwork(Map<String, Object> data, GatherAssetEs asset) {
        NetworkEs networkEs = new NetworkEs();
        networkEs.setId(CommUtils.createAuditId());
        networkEs.setDetail(data);
        networkEs.setAssetId(asset.getAssetId());
        networkEs.setGatherAssetId(asset.getId());
        networkEs.setName((String) data.get("name"));
        networkEs.setIp((String) data.get("ipAdd"));
        networkEs.setMac((String) data.get("macAdd"));
        networkEs.setMask((String) data.get("maskAdd"));
        networkEs.setUtime(asset.getUtime());
        return networkEs;
    }
}
