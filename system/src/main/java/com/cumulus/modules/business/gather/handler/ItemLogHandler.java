package com.cumulus.modules.business.gather.handler;

import com.cumulus.modules.business.gather.common.utils.CommUtils;
import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.entity.es.GatherInstance;
import com.cumulus.modules.business.gather.request.GatherItemLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 采集项解析处理接口
 *
 * @author zhaoff
 */
@Slf4j
public abstract class ItemLogHandler {

    /**
     * es索引
     */
    protected String esIndex;

    /**
     * 采集日志处理方法
     *
     * @return 该数据类型支持的资产类型列表
     */
    public abstract List<String> supportAssetTypes();

    /**
     * 采集日志处理方法
     *
     * @param asset    资产
     * @param itemLogs 采集日志
     * @throws Exception 异常
     */
    public abstract void handle(GatherAssetEs asset, Set<GatherItemLog> itemLogs) throws Exception;

    /**
     * 资产删除后相关处理
     *
     * @param asset 资产配置
     */
    public abstract void deleteHandle(GatherAssetEs asset);

    /**
     * 将解析的数据存入到资产详情中
     *
     * @param asset      采集资产
     * @param repository 数据接口
     * @param instances  实例列表
     * @param <T>        泛型
     */
    protected <T extends GatherInstance> void putDetails(GatherAssetEs asset,
                                                         ElasticsearchRepository<T, String> repository,
                                                         Collection<T> instances) {
        List<String> ids = new ArrayList<>();
        if (!CommUtils.isEmptyOfCollection(instances)) {
            instances.forEach(i -> {
                if (null != i.getId()) {
                    ids.add(i.getId());
                }
            });
            repository.saveAll(instances);
        }
        if (log.isInfoEnabled()) {
            log.info(String.format("esIndex:%s, instances size:%d, ids size:%d", getEsIndex(), instances.size(),
                    ids.size()));
        }
        Map<String, Object> data = new HashMap<>();
        data.put(GatherAssetEs.DETAILS_ITEM_LOG_IDS, ids);
        data.put(GatherAssetEs.DETAILS_ITEM_LOG_LEVEL, 0);
        data.put(GatherAssetEs.DETAILS_ITEM_LOG_FLAG, false);
        data.put(GatherAssetEs.DETAILS_ITEM_LOG_UTIME, asset.getUtime());
        asset.getDetails().put(esIndex, data);
    }

    /**
     * 获取该索引对应的资产采集详情
     *
     * @param asset 采集资产
     * @return 采集详情
     */
    @SuppressWarnings("unchecked")
    protected Map<String, Object> getDetails(GatherAssetEs asset) {
        return (Map<String, Object>) asset.getDetails().get(esIndex);
    }

    /**
     * 合并采集资产该索引指定的采集详情
     *
     * @param asset    上次采集资产
     * @param newAsset 最新采集资产
     */
    public void mergeAsset(GatherAssetEs asset, GatherAssetEs newAsset) {
        if (null == asset || null == newAsset) {
            return;
        }
        Object oldDetail = asset.getDetails().get(this.esIndex);
        Object newDetail = newAsset.getDetails().get(this.esIndex);
        if (null == newDetail && null != oldDetail) {
            newAsset.getDetails().put(this.esIndex, oldDetail);
        }
    }

    /**
     * 设置ES索引
     *
     * @param esIndex ES索引
     */
    public void setEsIndex(String esIndex) {
        this.esIndex = esIndex;
    }

    /**
     * 获取ES索引
     *
     * @return ES索引
     */
    public String getEsIndex() {
        return esIndex;
    }

}
