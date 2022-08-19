package com.cumulus.modules.business.gather.handler;

import com.cumulus.modules.business.gather.common.constant.GatherConstants;
import com.cumulus.modules.business.gather.common.utils.BusinessCommon;
import com.cumulus.modules.business.gather.common.utils.CommUtils;
import com.cumulus.modules.business.gather.entity.es.DiskPartitionEs;
import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.repository.DiskPartitionEsRepository;
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
 * 磁盘处理类
 *
 * @author : shenjc
 */
@Slf4j
@Component
public class DiskPartitionHandler extends ItemLogHandler {

    /**
     * 已装软件数据访问接口
     */
    @Autowired
    private DiskPartitionEsRepository repository;

    /**
     * 构造方法
     */
    public DiskPartitionHandler() {
        this.esIndex = GatherConstants.ES_INDEX_DISK;
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
        List<DiskPartitionEs> diskPartitions = new ArrayList<>();
        for (GatherItemLog itemLog : itemLogs) {
            if (null == itemLog) {
                continue;
            }
            String itemName = BusinessCommon.getNameFromItemKey(itemLog.getItemKey());
            Map<String, Object> elite = itemLog.getElite();
            if (null == elite || !elite.containsKey(itemName)) {
                continue;
            }
            List<Map<String, Object>> diskPartitionList = (List<Map<String, Object>>) elite.get(itemName);
            if (CommUtils.isEmptyOfCollection(diskPartitionList)) {
                continue;
            }
            for (Map<String, Object> data : diskPartitionList) {
                if (data.isEmpty()) {
                    continue;
                }
                DiskPartitionEs diskPartition = geDiskPartition(data, asset);
                diskPartitions.add(diskPartition);
            }
        }
        putDetails(asset, repository, diskPartitions);
    }

    @Override
    public void deleteHandle(GatherAssetEs asset) {
        repository.deleteByGatherAssetId(asset.getId());
    }

    /**
     * 生成磁盘信息
     *
     * @param data  采集数据
     * @param asset 资产
     * @return 软件
     */
    private DiskPartitionEs geDiskPartition(Map<String, Object> data, GatherAssetEs asset) {
        DiskPartitionEs diskPartition = new DiskPartitionEs();
        // 生成ID
        diskPartition.setId(CommUtils.createAuditId());
        diskPartition.setGatherAssetId(asset.getId());
        diskPartition.setAssetId(asset.getAssetId());
        diskPartition.setDetail(data);
        diskPartition.setUtime(asset.getUtime());
        String diskName = data.get("Filesystem") == null ? "" : String.valueOf(data.get("Filesystem"));
        diskPartition.setDiskName(diskName);
        return diskPartition;
    }
}
