package com.cumulus.modules.business.gather.provider.impl;

import com.cumulus.modules.business.entity.Asset;
import com.cumulus.modules.business.entity.AssetExtend;
import com.cumulus.modules.business.entity.AssetTag;
import com.cumulus.modules.business.gather.common.constant.GatherConstants;
import com.cumulus.modules.business.gather.common.utils.BusinessCommon;
import com.cumulus.modules.business.gather.common.utils.CommUtils;
import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.entity.es.GatherAssetLogEs;
import com.cumulus.modules.business.gather.handler.ItemLogHandler;
import com.cumulus.modules.business.gather.provider.BuildProvider;
import com.cumulus.modules.business.gather.request.GatherItemLog;
import com.cumulus.modules.business.repository.AssetTagRepository;
import com.cumulus.modules.system.dto.UserDto;
import com.cumulus.modules.system.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 构建资产抽象类
 *
 * @author zhaoff
 */
@Slf4j
public abstract class AbstractBuildProvider implements BuildProvider {

    @Autowired
    private AssetTagRepository assetTagRepository;

    @Autowired
    UserService userService;

    /**
     * es索引和关联itemKey的映射
     */
    protected Map<String, Set<String>> itemKeyMap = new HashMap<>();

    /**
     * es索引和采集日志处理类的映射
     */
    protected Map<String, ItemLogHandler> handlerMap = new HashMap<>();

    /**
     * 生成资产通用信息
     *
     * @param targetAsset 目前资产
     * @return 资产信息
     */
    @Override
    public GatherAssetEs initGatherAsset(GatherAssetLogEs assetLog, Asset targetAsset) {
        GatherAssetEs newAsset = new GatherAssetEs();
        newAsset.setAssetId(targetAsset.getId());
        newAsset.setName(targetAsset.getName());
        newAsset.setIp(targetAsset.getIp());
        newAsset.setAssetType(targetAsset.getAssetType().getName());
        newAsset.setAssetSysType(targetAsset.getAssetSysType().getId());
        newAsset.setAssetSysTypeName(targetAsset.getAssetSysType().getName());
        newAsset.setRiskLevel(targetAsset.getRiskLevel());
        newAsset.setPort(String.valueOf(targetAsset.getPort()));
        newAsset.setUpdateTime(targetAsset.getUpdateTime());
        newAsset.setProtocol(targetAsset.getProtocol());
        newAsset.setAccount(targetAsset.getAccount());
        newAsset.setFingerprint(targetAsset.getFingerprint());
        newAsset.setDiff(targetAsset.getAssetCategory());
        newAsset.setAssetStatus(targetAsset.getAssetStatus());
        newAsset.setFindTime(targetAsset.getCreateTime());
        if (null != targetAsset.getDept()) {
            newAsset.setDept(targetAsset.getDept().getName());
            UserDto deptHead = userService.getDeptHead(targetAsset.getDept().getId());
            if (null != deptHead) {
                newAsset.setLeader(deptHead.getUsername());
            }
        }
        List<AssetTag> tagList = assetTagRepository.findAllByAssetId(targetAsset.getId());
        if (null != tagList && !tagList.isEmpty()) {
            newAsset.setAssetTags(tagList.stream().map(AssetTag::getName).collect(Collectors.toList()));
        }
        if (null != targetAsset.getAssetExtend()) {
            AssetExtend assetExtend = targetAsset.getAssetExtend();
            newAsset.setUrl(assetExtend.getWebsite());
            newAsset.setServiceComponent(assetExtend.getServerComponent());
            newAsset.setHeadline(assetExtend.getTitle());
            newAsset.setLocation(assetExtend.getLocation());
        }
        if (null != assetLog) {
            newAsset.setUtime(assetLog.getEnd());
            newAsset.setTaskType(assetLog.getTaskType());
            newAsset.setStatus(GatherConstants.GATHER_ASSET_STATUS_ONLINE);
            Map<String, String> gatherAssetLogs = new HashMap<>();
            gatherAssetLogs.put(assetLog.getTaskType(), assetLog.getId());
            newAsset.getDetails().put(GatherAssetEs.DETAILS_ASSET_LOG, gatherAssetLogs);
            // 根据管理IP设置ipv6标记
            if (CommUtils.checkIPV6(targetAsset.getIp())) {
                newAsset.getGatherInfo().put(GatherAssetEs.GATHER_INFO_IPV6, true);
            } else {
                newAsset.getGatherInfo().put(GatherAssetEs.GATHER_INFO_IPV6, false);
            }
        } else {
            // level设置为未采集
            newAsset.setRiskLevel(GatherConstants.RISK_LEVEL_NOT_COLLECTED);
        }
        return newAsset;
    }

    @Override
    public GatherAssetEs build(GatherAssetEs gatherAsset, GatherAssetLogEs gatherAssetLog, Asset asset) {
        if (null == gatherAssetLog) {
            return null;
        }
        gatherAsset.getGatherInfo().put(GatherAssetEs.GATHER_INFO_VERSION, gatherAssetLog.getVersion());
        Map<String, Set<GatherItemLog>> classifyItemLogs = new HashMap<>();
        List<GatherItemLog> itemLogs = gatherAssetLog.getItemlogs();
        for (GatherItemLog itemLog : itemLogs) {
            for (Map.Entry<String, Set<String>> entry : itemKeyMap.entrySet()) {
                boolean match = false;
                Set<String> matchKeys = entry.getValue();
                for (String key : matchKeys) {
                    if (itemKeyMatch(itemLog.getItemKey(), key)) {
                        match = true;
                        break;
                    }
                }
                if (match) {
                    Set<GatherItemLog> matchItems =
                            classifyItemLogs.computeIfAbsent(entry.getKey(), k -> new HashSet<>());
                    matchItems.add(itemLog);
                }
            }
        }
        for (Map.Entry<String, Set<GatherItemLog>> entry : classifyItemLogs.entrySet()) {
            ItemLogHandler handler = handlerMap.get(entry.getKey());
            if (null == handler) {
                if (log.isErrorEnabled()) {
                    log.error(String.format("ItemLogHandler not exist, type:%s, assetName:%s", entry.getKey(),
                            gatherAssetLog.getAssetName()));
                }
                continue;
            }
            LocalDateTime start = LocalDateTime.now();
            try {
                handler.handle(gatherAsset, entry.getValue());
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error(String.format("failed to handle item logs, type:%s, assetName:%s", entry.getKey(),
                            gatherAssetLog.getAssetName()), e);
                }
            } finally {
                LocalDateTime end = LocalDateTime.now();
                if (log.isDebugEnabled()) {
                    log.info(String.format("parse gather data for %s, start:%s, end:%s.",
                            handler.getEsIndex(), start, end));
                }
            }
        }
        return gatherAsset;
    }

    @Override
    public void deleteHandle(GatherAssetEs asset) {
        Map<String, Object> details = asset.getDetails();
        for (Map.Entry<String, Object> entry : details.entrySet()) {
            ItemLogHandler handler = this.handlerMap.get(entry.getKey());
            if (null == handler) {
                continue;
            }
            handler.deleteHandle(asset);
        }
    }

    /**
     * 合并新采集资产和上一次采集资产的配置信息
     *
     * @param asset    上一次采集资产
     * @param newAsset 最新采集的资产
     */
    @SuppressWarnings("unchecked")
    @Override
    public void mergeAsset(GatherAssetEs asset, GatherAssetEs newAsset) {
        try {
            BusinessCommon.merge(newAsset, asset);
            for (ItemLogHandler handler : handlerMap.values()) {
                handler.mergeAsset(asset, newAsset);
            }
            Map<String, Object> oldLogs =
                    (Map<String, Object>) asset.getDetails().get(GatherAssetEs.DETAILS_ASSET_LOG);
            Map<String, Object> newLogs =
                    (Map<String, Object>) newAsset.getDetails().get(GatherAssetEs.DETAILS_ASSET_LOG);
            if (!CommUtils.isEmptyOfMap(oldLogs)) {
                for (Map.Entry<String, Object> entry : oldLogs.entrySet()) {
                    if (!newLogs.containsKey(entry.getKey())) {
                        newLogs.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            // 设置版本号
            String version = newAsset.getVersion();
            if (newAsset.getAssetType().equals(GatherConstants.TYPE_APPLICATION)
                    || newAsset.getAssetType().equals(GatherConstants.TYPE_MIDDLEWARE)
                    || newAsset.getAssetType().equals(GatherConstants.TYPE_DATABASE)) {
                version = newAsset.getVersion().replace(newAsset.getAssetSysType() + " ", "");
            }
            newAsset.getGatherInfo().put(GatherAssetEs.GATHER_INFO_VERSION_NUMBER, version);
            // 合并采集信息
            for (Map.Entry<String, Object> entry : asset.getGatherInfo().entrySet()) {
                if (newAsset.getGatherInfo().containsKey(entry.getKey())) {
                    continue;
                }
                newAsset.getGatherInfo().put(entry.getKey(), entry.getValue());
            }
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn(String.format("failed to merge asset, id:%s, name:%s.", asset.getId(), asset.getName()), e);
            }
        }
    }

    /**
     * 存es前需要将itemKey中“.”替换成“_”
     *
     * @param itemLogs 采集日志
     */
    private void modifyItemKey(List<GatherItemLog> itemLogs) {
        for (GatherItemLog itemLog : itemLogs) {
            itemLog.setItemKey(itemLog.getItemKey().replace(".", "_"));
        }
    }

    /**
     * 使用正则匹配itemKey
     *
     * @param itemKey  被匹配的itemKey
     * @param matchKey 正则表达式
     * @return 匹配结果
     */
    private boolean itemKeyMatch(String itemKey, String matchKey) {
        if (itemKey.contains(matchKey)) {
            return true;
        }
        if (matchKey.contains("*") || matchKey.contains("$")) {
            String regex = matchKey.replaceAll("\\*", ".*");
            return itemKey.matches(regex);
        }
        return itemKey.contains(matchKey);
    }

}
