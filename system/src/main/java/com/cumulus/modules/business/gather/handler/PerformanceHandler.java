package com.cumulus.modules.business.gather.handler;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cumulus.modules.business.gather.common.constant.GatherConstants;
import com.cumulus.modules.business.gather.common.utils.BusinessCommon;
import com.cumulus.modules.business.gather.common.utils.CommUtils;
import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.entity.es.PerformanceEs;
import com.cumulus.modules.business.gather.repository.PerformanceEsRepository;
import com.cumulus.modules.business.gather.request.GatherItemLog;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 性能监控处理类
 *
 * @author Shijh
 */
@Slf4j
@Component
public class PerformanceHandler extends ItemLogHandler {

    /**
     * 性能监控数据访问接口
     */
    @Autowired
    private PerformanceEsRepository repository;

    /**
     * 构造方法
     */
    public PerformanceHandler() {
        this.esIndex = GatherConstants.ES_INDEX_PERFORMANCE;
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
        List<PerformanceEs> performances = new ArrayList<>();
        HashMap<String, Object> map = new HashMap<>();
        for (GatherItemLog itemLog : itemLogs) {
            if (null == itemLog) {
                continue;
            }
            String itemName = BusinessCommon.getNameFromItemKey(itemLog.getItemKey());
            Map<String, Object> elite = itemLog.getElite();
            if (null == elite || !elite.containsKey(itemName)) {
                continue;
            }
            if ("diskpartition".equals(itemName)) {
                continue;
            }
            Map<String, Object> performanceList = (Map<String, Object>) elite.get(itemName);
            if (CommUtils.isEmptyAnyMap(performanceList)) {
                continue;
            }
            map.putAll(performanceList);
        }
        if (!map.isEmpty()){
            PerformanceEs performance = genPerformance(map, asset);
            performances.add(performance);
            putDetails(asset, repository, performances);
        }
    }

    /**
     * 生成性能信息
     *
     * @param data  采集数据
     * @param asset 资产
     * @return 性能
     */
    private PerformanceEs genPerformance(Map<String, Object> data, GatherAssetEs asset) {
        PerformanceEs performance = new PerformanceEs();
        performance.setId(CommUtils.createAuditId());
        performance.setGatherAssetId(asset.getId());
        performance.setUtime(asset.getUtime());
        performance.setDetail(data);
        DecimalFormat df = new DecimalFormat("0.00");
        Object ioWait = data.get("iowait");
        if (null == ioWait || StringUtils.isBlank(ioWait.toString())) {
            performance.setIo("0");
        } else {
            performance.setIo(data.get("iowait").toString());
        }
        String format = df.format((double) Integer.parseInt(data.get("free").toString()) / Integer.parseInt(data.get("total").toString()) * 100.00);
        performance.setMemory(format);
        performance.setCpu(data.get("us").toString());
        return performance;
    }

    @Override
    public void deleteHandle(GatherAssetEs asset) {
        repository.deleteByGatherAssetId(asset.getId());
    }
}
