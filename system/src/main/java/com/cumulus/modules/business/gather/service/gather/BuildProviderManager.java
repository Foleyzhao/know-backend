package com.cumulus.modules.business.gather.service.gather;

import com.cumulus.modules.business.gather.provider.BuildProvider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 资产采集数据解析器管理器
 *
 * @author zhaoff
 */
@Component
public class BuildProviderManager implements ApplicationContextAware {

    /**
     * 资产采集数据解析器集合
     */
    private final Map<String, BuildProvider> buildProviderMap = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        List<BuildProvider> list = new ArrayList<>(
                BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, BuildProvider.class).values());
        list.forEach(b -> {
            if (null != b.getCategory()) {
                this.buildProviderMap.put(b.getCategory(), b);
            } else if (null != b.getMainCategory()) {
                this.buildProviderMap.put(b.getMainCategory(), b);
            }
        });
    }

    /**
     * 根据资产类型获取资产采集数据解析器
     *
     * @param assetType    资产一级分类
     * @param assetSysType 资产二级分类
     * @return 资产采集数据解析器
     */
    public BuildProvider getBuildProvider(String assetType, String assetSysType) {
        BuildProvider provider = null;
        if (null != assetSysType) {
            provider = buildProviderMap.get(assetSysType);
        }
        if (null == provider && null != assetType) {
            provider = buildProviderMap.get(assetType);
        }
        return provider;
    }

}
