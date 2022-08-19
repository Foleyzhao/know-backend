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
import com.cumulus.modules.business.gather.entity.es.SystemProcessEs;
import com.cumulus.modules.business.gather.repository.SystemProcessEsRepository;
import com.cumulus.modules.business.gather.request.GatherItemLog;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 系统进程处理类
 *
 * @author Shijh
 */
@Slf4j
@Component
public class SystemProcessHandler extends ItemLogHandler {

    /**
     * 服务进程数据访问接口
     */
    @Autowired
    private SystemProcessEsRepository repository;

    /**
     * 构造方法
     */
    public SystemProcessHandler() {
        this.esIndex = GatherConstants.ES_INDEX_PROCESS;
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
        List<SystemProcessEs> systemProcesss = new ArrayList<>();
        for (GatherItemLog itemLog : itemLogs) {
            if (null == itemLog) {
                continue;
            }
            String itemName = BusinessCommon.getNameFromItemKey(itemLog.getItemKey());
            Map<String, Object> elite = itemLog.getElite();
            if (null == elite || !elite.containsKey(itemName)) {
                continue;
            }
            List<Map<String, Object>> accountList = (List<Map<String, Object>>) elite.get(itemName);
            if (CommUtils.isEmptyOfCollection(accountList)) {
                continue;
            }
            for (Map<String, Object> data : accountList) {
                if (data.isEmpty()) {
                    continue;
                }
                SystemProcessEs systemProcessEs = genSystemProcess(data, asset);
                systemProcesss.add(systemProcessEs);
            }
        }
        putDetails(asset, repository, systemProcesss);
    }

    /**
     * 系统进程信息
     *
     * @param data  采集数据
     * @param asset 资产
     * @return 系统进程
     */
    private SystemProcessEs genSystemProcess(Map<String, Object> data, GatherAssetEs asset) {
        SystemProcessEs systemProcess = new SystemProcessEs();
        systemProcess.setId(CommUtils.createAuditId());
        systemProcess.setGatherAssetId(asset.getId());
        systemProcess.setUtime(asset.getUtime());
        systemProcess.setUser(data.get("user").toString());
        systemProcess.setCpu(data.get("cpu").toString());
        systemProcess.setMem(data.get("mem").toString());
        systemProcess.setState(data.get("state").toString());
        systemProcess.setParentProcess(data.get("ppid").toString());
        systemProcess.setProcess(data.get("name").toString());
        systemProcess.setExecuteTime(data.get("time").toString());
        return systemProcess;
    }

    @Override
    public void deleteHandle(GatherAssetEs asset) {
        repository.deleteByGatherAssetId(asset.getId());
    }

}
