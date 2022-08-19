package com.cumulus.modules.business.gather.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cumulus.modules.business.gather.common.constant.GatherConstants;
import com.cumulus.modules.business.gather.common.utils.BusinessCommon;
import com.cumulus.modules.business.gather.common.utils.CommUtils;
import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.entity.es.PortEs;
import com.cumulus.modules.business.gather.repository.PortEsRepository;
import com.cumulus.modules.business.gather.request.GatherItemLog;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 端口处理类
 *
 * @author Shijh
 */
@Slf4j
@Component
public class PortHandler extends ItemLogHandler {

    /**
     * 端口数据访问接口
     */
    @Autowired
    private PortEsRepository repository;

    /**
     * 构造方法
     */
    public PortHandler() {
        this.esIndex = GatherConstants.ES_INDEX_NETSTAT;
    }

    @Override
    public List<String> supportAssetTypes() {
        return Collections.singletonList(GatherConstants.TYPE_HOST.toString());
    }

    @Override
    public void handle(GatherAssetEs asset, Set<GatherItemLog> itemLogs) {
        if (null == asset || CommUtils.isEmptyOfCollection(itemLogs)) {
            return;
        }
        List<PortEs> ports = new ArrayList<>();
        for (GatherItemLog itemLog : itemLogs) {
            if (null == itemLog) {
                continue;
            }
            String itemName = BusinessCommon.getNameFromItemKey(itemLog.getItemKey());
            Map<String, Object> elite = itemLog.getElite();
            if (null == elite || !elite.containsKey(itemName)) {
                continue;
            }
            List<Map<String, Object>> portList = (List<Map<String, Object>>) elite.get(itemName);
            if (CommUtils.isEmptyOfCollection(portList)) {
                continue;
            }
            for (Map<String, Object> data : portList) {
                if (data.isEmpty()) {
                    continue;
                }
                PortEs port = genAccount(data, asset);
                ports.add(port);
            }
        }
        putDetails(asset, repository, ports);
    }

    /**
     * 生成端口信息
     *
     * @param data  采集数据
     * @param asset 资产
     * @return 端口
     */
    private PortEs genAccount(Map<String, Object> data, GatherAssetEs asset) {
        PortEs port = new PortEs();
        port.setId(CommUtils.createAuditId());
        port.setGatherAssetId(asset.getId());
        port.setUtime(asset.getUtime());
        port.setDetail(data);
        port.setProtocol((String) data.get("proto"));
        port.setState((String) data.get("serStatus"));
        port.setProgram((String) data.get("program"));
        port.setLocalAddress((String) data.get("localAdd"));
        port.setLocalPort((String) data.get("localPort"));
        port.setExternalAddress((String) data.get("externalAdd"));
        port.setExternalPort((String) data.get("externalPort"));
        port.setName(Integer.parseInt(data.get("localPort").toString()));
        Object pid = data.get("pid");
        if (pid != null) {
            port.setProcessId(Integer.parseInt((String) pid));
        }
        return port;
    }

    @Override
    public void deleteHandle(GatherAssetEs asset) {
        repository.deleteByGatherAssetId(asset.getId());
    }
}
