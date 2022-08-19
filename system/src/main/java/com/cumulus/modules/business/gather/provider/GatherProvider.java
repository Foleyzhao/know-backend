package com.cumulus.modules.business.gather.provider;

import com.cumulus.modules.business.gather.request.GatherAssetRequest;
import com.cumulus.modules.business.gather.request.GatherException;

/**
 * 采集功能的提供者接口
 *
 * @author zhaoff
 */
public interface GatherProvider {

    /**
     * 返回支持的采集功能主分类
     *
     * @return 支持的采集功能主要分类
     */
    String getMainCategory();

    /**
     * 返回支持的采集功能次分类
     *
     * @return 支持的采集功能次分类
     */
    String getCategory();

    /**
     * 执行采集操作，并将结果填入request参数中
     *
     * @param request 采集请求
     * @throws GatherException 采集异常
     */
    void execute(GatherAssetRequest request) throws GatherException;

}
