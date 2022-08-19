package com.cumulus.modules.business.gather.provider;

import com.cumulus.modules.business.entity.Asset;
import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.entity.es.GatherAssetLogEs;

/**
 * 资产采集数据解析接口
 *
 * @author zhaoff
 */
public interface BuildProvider {

    /**
     * 返回支持的采集功能主分类
     *
     * @return 支持的采集功能主分类
     */
    String getMainCategory();

    /**
     * 返回支持的采集功能次分类
     *
     * @return 支持的采集功能次分类
     */
    String getCategory();

    /**
     * 初始化采集资产信息
     *
     * @param gatherAssetLogES 资产采集日志
     * @param asset            资产
     * @return 采集资产
     */
    GatherAssetEs initGatherAsset(GatherAssetLogEs gatherAssetLogES, Asset asset);

    /**
     * 构建采集资产信息
     *
     * @param gatherAssetEs    采集资产
     * @param gatherAssetLogES 资产采集日志
     * @param asset            资产
     * @return 构建采集完成的采集资产信息
     */
    GatherAssetEs build(GatherAssetEs gatherAssetEs, GatherAssetLogEs gatherAssetLogES, Asset asset);

    /**
     * 合并新采集资产和上一次采集资产的配置信息
     *
     * @param asset    上一次采集资产
     * @param newAsset 最新采集的资产
     */
    void mergeAsset(GatherAssetEs asset, GatherAssetEs newAsset);

    /**
     * 删除资产相关处理
     *
     * @param asset 资产采集记录
     */
    void deleteHandle(GatherAssetEs asset);

    /**
     * es索引和关联itemKey的映射
     *
     */
    void getItemKeyMap();

}
