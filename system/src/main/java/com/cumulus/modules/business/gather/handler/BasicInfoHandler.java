package com.cumulus.modules.business.gather.handler;

import com.cumulus.modules.business.gather.common.constant.GatherConstants;
import com.cumulus.modules.business.gather.common.utils.BusinessCommon;
import com.cumulus.modules.business.gather.common.utils.CommUtils;
import com.cumulus.modules.business.gather.entity.es.BasicInfoEs;
import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.repository.BasicInfoEsRepository;
import com.cumulus.modules.business.gather.request.GatherItemLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 基本信息处理类
 *
 * @author : shenjc
 */
@Slf4j
@Component
public class BasicInfoHandler extends ItemLogHandler {

    private static final String RUNTIME = "runtime";
    private static final String OS_VENDOR = "osvendor";
    private static final String UUID = "uuid";
    private static final String HOSTNAME = "hostname";
    private static final String OS_VERSION = "osversion";
    private static final String KERNEL = "kernel";


    /**
     * 基本信息ES 数据接口
     */
    @Autowired
    private BasicInfoEsRepository repository;

    public BasicInfoHandler() {
        this.esIndex = GatherConstants.ES_INDEX_BASIC_INFO;
    }

    @Override
    public List<String> supportAssetTypes() {
        return Collections.singletonList(GatherConstants.TYPE_HOST.toString());
    }

    @Override
    public void handle(GatherAssetEs asset, Set<GatherItemLog> itemLogs) throws Exception {
        BasicInfoEs basicInfo = new BasicInfoEs();
        basicInfo.setId(CommUtils.createAuditId());
        basicInfo.setAssetId(asset.getAssetId());
        basicInfo.setGatherAssetId(asset.getId());
        basicInfo.setUtime(asset.getUtime());
        Map<String, Object> detail = new HashMap<>(itemLogs.size());
        basicInfo.setDetail(detail);
        for (GatherItemLog itemLog : itemLogs) {
            if (Objects.equals(itemLog.getResult(), 0)) {
                handleBasicInfo(basicInfo, itemLog, detail);
            }
        }
        putDetails(asset, repository, Collections.singletonList(basicInfo));
    }

    @Override
    public void deleteHandle(GatherAssetEs asset) {
        repository.deleteByGatherAssetId(asset.getId());
    }

    /**
     * 处理基本信息
     *
     * @param basicInfo 对象
     * @param itemLog   数据
     * @param detail    原始数据map
     */
    public void handleBasicInfo(BasicInfoEs basicInfo, GatherItemLog itemLog, Map<String, Object> detail) {
        String replaceKey = BusinessCommon.eraseSysFromItemkey(itemLog.getItemKey());
        replaceKey = replaceKey.replace(".", "_");
        String itemName = BusinessCommon.getNameFromItemKey(itemLog.getItemKey());
        Object value = itemLog.getElite().get(itemName);
        if (null == itemLog.getElite() || !itemLog.getElite().containsKey(itemName)) {
            return;
        }
        switch (itemName) {
            case RUNTIME: {
                basicInfo.setRunTime(HardwareInfoHandler.getRuntime((String) value));
                detail.put(replaceKey, value);
                break;
            }
            case UUID: {
                basicInfo.setUuid((String) value);
                detail.put(replaceKey, value);
                break;
            }
            case HOSTNAME: {
                basicInfo.setHostname((String) value);
                detail.put(replaceKey, value);
                break;
            }
            default: {
                detail.put(replaceKey, value);
                break;
            }
        }
    }
}
