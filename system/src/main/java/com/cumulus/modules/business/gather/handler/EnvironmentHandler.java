package com.cumulus.modules.business.gather.handler;

import com.cumulus.modules.business.gather.common.constant.GatherConstants;
import com.cumulus.modules.business.gather.common.utils.BusinessCommon;
import com.cumulus.modules.business.gather.common.utils.CommUtils;
import com.cumulus.modules.business.gather.entity.es.EnvironmentEs;
import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.repository.EnvironmentEsRepository;
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
 * 环境变量处理类
 *
 * @author : shenjc
 */
@Slf4j
@Component
public class EnvironmentHandler extends ItemLogHandler {

    @Autowired
    private EnvironmentEsRepository repository;

    public EnvironmentHandler() {
        this.esIndex = GatherConstants.ES_INDEX_ENVIRONMENT;
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
        List<EnvironmentEs> environmentEs = new ArrayList<>();
        for (GatherItemLog itemLog : itemLogs) {
            if (null == itemLog) {
                continue;
            }
            String itemName = BusinessCommon.getNameFromItemKey(itemLog.getItemKey());
            Map<String, Object> elite = itemLog.getElite();
            if (null == elite || !elite.containsKey(itemName)) {
                continue;
            }
            if (elite.get(itemName) != null && elite.get(itemName) instanceof Map) {
                Map<String, Object> environmentMap = (Map<String, Object>) elite.get(itemName);
                if (CommUtils.isEmptyOfMap(environmentMap)) {
                    continue;
                }
                for (Map.Entry<String, Object> entry : environmentMap.entrySet()) {
                    if (entry.getValue() != null && entry.getValue() instanceof List) {
                        List<String> environmentList = (List<String>) entry.getValue();
                        if (CommUtils.isEmptyOfCollection(environmentList)) {
                            continue;
                        }
                        for (String value : environmentList) {
                            EnvironmentEs environment = getEnvironment(entry.getKey(), itemName, value, asset);
                            environmentEs.add(environment);
                        }
                    }
                }
            }
        }
        putDetails(asset, repository, environmentEs);
    }

    @Override
    public void deleteHandle(GatherAssetEs asset) {
        repository.deleteByGatherAssetId(asset.getId());
    }

    /**
     * 生成环境信息
     *
     * @param type  环境变量类型 目前存在 usertpye 和 systype 2种
     * @param name  环境变量名
     * @param value 环境变量值
     * @param asset 资产
     * @return 软件
     */
    private EnvironmentEs getEnvironment(String type, String name, String value, GatherAssetEs asset) {
        EnvironmentEs environment = new EnvironmentEs();
        environment.setId(CommUtils.createAuditId());
        environment.setAssetId(asset.getAssetId());
        environment.setGatherAssetId(asset.getId());
        environment.setUtime(asset.getUtime());
        environment.setName(name);
        environment.setType(type);
        environment.setValue(value);
        return environment;
    }


}
