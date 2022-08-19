package com.cumulus.modules.business.gather.service.gather;

import com.cumulus.modules.business.entity.Asset;
import com.cumulus.modules.business.gather.common.constant.GatherConstants;
import com.cumulus.modules.business.gather.common.service.AmqpNotificationService;
import com.cumulus.modules.business.gather.common.utils.CommUtils;
import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.entity.es.GatherAssetLogEs;
import com.cumulus.modules.business.gather.provider.BuildProvider;
import com.cumulus.modules.business.gather.repository.GatherAssetLogEsRepository;
import com.cumulus.modules.business.gather.service.GatherAssetEsService;
import com.cumulus.modules.business.service.AssetService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 采集数据处理服务接实现ES）
 *
 * @author zhaoff
 */
@Slf4j
@Service
public class GatherDataEsServiceImpl implements GatherDataEsService {

    /**
     * 内部通知使用的服接口
     */
    @Autowired
    private AmqpNotificationService notificationService;

    /**
     * 资产服务接口
     */
    @Autowired
    private AssetService assetService;

    /**
     * 采集资产服务接口
     */
    @Autowired
    private GatherAssetEsService gatherAssetEsService;

    /**
     * 资产采集数据解析器管理器
     */
    @Autowired
    private BuildProviderManager buildManager;

    /**
     * (采集)指标维护类
     */
    @Autowired
    private GatherCenter gatherCenter;

    /**
     * 采集控制管理
     */
    @Autowired
    private GatherControl gatherControl;

    /**
     * 资产采集日志数据访问接口
     */
    @Autowired
    private GatherAssetLogEsRepository gatherAssetLogEsRepository;

    /**
     * 采集日志解析调度器
     */
    private ExecutorService dataParseExecutor = null;

    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        ThreadFactoryBuilder parseThreadBuilder = new ThreadFactoryBuilder();
        parseThreadBuilder.setNameFormat("GatherData-Parser-%d");
        parseThreadBuilder.setDaemon(true);
        dataParseExecutor = Executors.newFixedThreadPool(CommUtils.getGatherJobNum(), parseThreadBuilder.build());
    }

    @Override
    public void saveOrUpdate(String gatherId) {
        if (log.isInfoEnabled()) {
            log.info("gather end for id:" + gatherId);
        }
        dataParseExecutor.submit(new GatherDataParser(gatherId));
    }

    /**
     * 发送MQ消息通知资产采集完成
     *
     * @param oldAsset 上一次采集的资产信息
     * @param newAsset 本次采集资产信息
     * @param assetLog 本次采集日志
     */
    @SuppressWarnings("unchecked")
    private void notifyGatherFinish(GatherAssetEs oldAsset, GatherAssetEs newAsset, GatherAssetLogEs assetLog) {
        if (newAsset.isIntegrated()) {
            // 采集完成变更后发送消息
            Set<String> logIds = new HashSet<>();
            if (null == oldAsset || !oldAsset.isIntegrated()) {
                // 首次完整采集
                Map<String, Object> assetLogMap =
                        (Map<String, Object>) newAsset.getDetails().get(GatherAssetEs.DETAILS_ASSET_LOG);
                if (!CommUtils.isEmptyOfMap(assetLogMap)) {
                    assetLogMap.values().forEach(v -> logIds.add((String) v));
                }
            }
            if (logIds.isEmpty()) {
                logIds.add(assetLog.getId());
            }
            logIds.forEach(id -> {
                Map<String, Object> msg = new HashMap<>();
                msg.put(AmqpNotificationService.MSG_UPDATE_FINISHED_CONTENT_GATHERASSETID, newAsset.getId());
                msg.put(AmqpNotificationService.MSG_UPDATE_FINISHED_CONTENT_ASSETLOGID, id);
                msg.put(AmqpNotificationService.MSG_UPDATE_FINISHED_CONTENT_ASSETID, newAsset.getAssetId());
                msg.put("planId", assetLog.getPlanId());
                msg.put("planName", assetLog.getPlanName());
                msg.put("begin", assetLog.getBegin());
                msg.put("result", assetLog.getResult());
                msg.put("assetIp", assetLog.getAssetIp());
                msg.put("flag", assetLog.getFlag());
                notificationService.sendNotification(AmqpNotificationService.MSG_UPDATE_FINISHED, msg);
            });
        }
        if (log.isInfoEnabled()) {
            log.info(String.format("device %s assetId %s gather log process finished.", assetLog.getAssetIp(),
                    newAsset.getId()));
        }
    }

    /**
     * 资产所有类型的采集数据拼装完成
     *
     * @param asset 资产
     * @return 是否数据拼装完成
     */
    @SuppressWarnings("unchecked")
    private boolean assetIntegrated(GatherAssetEs asset) {
        Set<Integer> assetTaskTypes = gatherCenter.getCollectTypesBySysType(asset.getAssetSysType());
        Map<String, Object> assetLogMap = (Map<String, Object>) asset.getDetails().get(GatherAssetEs.DETAILS_ASSET_LOG);
        return !CommUtils.isEmptyOfMap(assetLogMap) && assetLogMap.size() >= assetTaskTypes.size();
    }

    /**
     * 采集日志解析线程
     *
     * @author zhaoff
     */
    private class GatherDataParser implements Runnable {

        /**
         * 采集ID
         */
        String gatherId;

        /**
         * 构造方法
         *
         * @param gatherId 采集ID
         */
        public GatherDataParser(String gatherId) {
            this.gatherId = gatherId;
        }

        @Override
        public void run() {
            try {
                GatherAssetLogEs assetLog = gatherAssetLogEsRepository.findByGatherId(gatherId);
                if (null == assetLog) {
                    if (log.isErrorEnabled()) {
                        log.error(String.format("gather asset log not exist, gather id %s.", gatherId));
                    }
                    return;
                }
                saveOrUpdateAsset(assetLog);
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error(String.format("parse gather data exception, gatherId:%s.", gatherId), e);
                }
            }
        }

        /**
         * 更新资产详情
         *
         * @param assetLog 采集日志
         */
        @Transactional(rollbackFor = Exception.class)
        private void saveOrUpdateAsset(GatherAssetLogEs assetLog) {
            Long assetId = assetLog.getAssetId();
            GatherAssetEs oldGatherAsset;
            final AtomicBoolean controlAcquired = new AtomicBoolean();
            controlAcquired.set(false);
            try {
                Asset asset = assetService.findById(assetId);
                if (null == asset) {
                    if (log.isWarnEnabled()) {
                        log.warn(String.format("Asset %s has been deleted.", assetLog.getAssetName()));
                    }
                    return;
                }
                if (log.isDebugEnabled()) {
                    log.debug(String.format("start to get lock for gatherId:%s, name:%s, taskType:%s.", gatherId,
                            asset.getName(), assetLog.getTaskType()));
                }
                int tryLockCount = 1;
                while (tryLockCount <= 6) {
                    try {
                        if (gatherControl.lock(asset.getId(), 60L)) {
                            if (log.isInfoEnabled()) {
                                log.info(String.format("succeed to get lock for gatherId:%s, name:%s.", gatherId,
                                        asset.getName()));
                            }
                            controlAcquired.set(true);
                            break;
                        } else {
                            if (log.isInfoEnabled()) {
                                log.info(String.format("wait for lock timeout, gatherId:%s, name:%s.", gatherId,
                                        asset.getName()));
                            }
                        }
                    } catch (Exception e) {
                        //上一次采集的处理时间过长，可能会导致数据冲突
                        if (log.isErrorEnabled()) {
                            log.error(String.format("failed to get lock for gatherId:%s, name:%s.", gatherId,
                                    asset.getName()), e);
                        }
                    } finally {
                        tryLockCount++;
                    }
                }
                oldGatherAsset = gatherAssetEsService.findGatherAssetByAssetId(assetId);
                if (GatherConstants.RESULT_FAIL.equals(assetLog.getResult())) {
                    // 采集失败时,不记录资产日志
                    gatherFailure(oldGatherAsset);
                    return;
                }
                BuildProvider buildProvider = getBuildProvider(asset);
                GatherAssetEs newAsset = buildProvider.initGatherAsset(assetLog, asset);
                if (null != oldGatherAsset) {
                    newAsset.setId(oldGatherAsset.getId());
                    buildProvider.build(newAsset, assetLog, asset);
                    try {
                        buildProvider.mergeAsset(oldGatherAsset, newAsset);
                    } catch (Exception e) {
                        if (log.isErrorEnabled()) {
                            log.error(String.format("failed to merge gatherAsset, gatherId:%s, assetName:%s",
                                    assetLog.getGatherId(), assetLog.getAssetName()), e);
                        }
                        throw e;
                    }
                } else {
                    newAsset.setId(CommUtils.createAuditId());
                    buildProvider.build(newAsset, assetLog, asset);
                }
                newAsset.setIntegrated(assetIntegrated(newAsset));
                gatherAssetEsService.save(newAsset);
                asset.setGatherAssetId(newAsset.getId());
                assetService.updateGatherAssetIdById(asset.getId(), newAsset.getId());
                notifyGatherFinish(oldGatherAsset, newAsset, assetLog);
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error(String.format("failed to parser gather asset log, gatherId:%s.",
                            assetLog.getGatherId()), e);
                }
            } finally {
                if (controlAcquired.get()) {
                    if (log.isInfoEnabled()) {
                        log.info(String.format("release lock, assetId: %s.", assetId));
                    }
                    gatherControl.unlock(assetId);
                }
            }
        }

        /**
         * 获取资产采集数据解析器
         *
         * @param asset 目标资产
         * @return 资产采集数据解析器
         */
        private BuildProvider getBuildProvider(Asset asset) {
            if (GatherConstants.TYPE_NETWORK.toString().equals(asset.getAssetType().getId().toString())
                    || GatherConstants.TYPE_SECURITY.toString().equals(asset.getAssetType().getId().toString())) {
                // 网络、安全设备
                return buildManager.getBuildProvider(GatherConstants.TYPE_NETWORK.toString(),
                        asset.getAssetSysType().getName());
            } else {
                return buildManager.getBuildProvider(asset.getAssetType().getId().toString(),
                        asset.getAssetSysType().getName());
            }
        }

        /**
         * 处理采集失败
         *
         * @param gatherAsset 上一次采集后的资产详情
         */
        private void gatherFailure(GatherAssetEs gatherAsset) {
            if (null == gatherAsset) {
                return;
            }
            try {
                // 采集失败时,不记录资产日志
                gatherAsset.setStatus(GatherConstants.GATHER_ASSET_STATUS_OFFLINE);
                gatherAssetEsService.save(gatherAsset);
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error("gather failed asset:" + gatherAsset.getIp(), e);
                }
                return;
            }
            if (log.isInfoEnabled()) {
                log.info("gather failed asset:" + gatherAsset.getIp());
            }
        }
    }
}
