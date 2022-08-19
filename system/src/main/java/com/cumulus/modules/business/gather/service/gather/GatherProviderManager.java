package com.cumulus.modules.business.gather.service.gather;

import com.cumulus.modules.business.gather.common.utils.CommUtils;
import com.cumulus.modules.business.gather.provider.GatherProvider;
import com.cumulus.modules.business.gather.request.GatherAssetRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 资产采集数据采集器管理器
 *
 * @author zhaoff
 */
@Slf4j
@Service
public class GatherProviderManager implements ApplicationContextAware {

    /**
     * 资产采集数据采集器集合
     */
    private final Map<String, Map<String, GatherProvider>> gatherProviderMap = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        List<GatherProvider> list = new ArrayList<>(
                BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, GatherProvider.class).values());
        for (GatherProvider provider : list) {
            String mainCategory = provider.getMainCategory().toLowerCase();
            Map<String, GatherProvider> categoryMap = gatherProviderMap.get(mainCategory);
            if (null == categoryMap) {
                categoryMap = new ConcurrentHashMap<>();
                gatherProviderMap.put(mainCategory, categoryMap);
            }
            String category = provider.getCategory().toLowerCase();
            GatherProvider oldProvider = categoryMap.get(category);
            if (null != oldProvider) {
                if (log.isWarnEnabled()) {
                    log.warn(String.format("Category '%s.%s' has been implemented in '%s', provider '%s' is skipped.",
                            mainCategory, category, oldProvider, provider));
                }
                continue;
            }
            categoryMap.put(category, provider);
        }
    }

    /**
     * 执行采集操作
     *
     * @param request 资产采集请求
     */
    public void execute(GatherAssetRequest request) {
        GatherProvider provider = null;
        String mainCategory = request.getMainCategory();
        Map<String, GatherProvider> providersMap = gatherProviderMap.get(mainCategory.toLowerCase());
        if (!CommUtils.isEmptyOfMap(providersMap)) {
            provider = providersMap.get(request.getCategory().toLowerCase());
        }

        if (null == provider) {
            request.setSuccess(false);
            request.setErrorMsg(String.format("Cannot find provider for %s.%s", request.getMainCategory(),
                    request.getCategory()));
            if (log.isDebugEnabled()) {
                log.debug(String.format("Provider map info:<%s>, request mainCategory:<%s>, request category:<%s>",
                        providersMap, request.getMainCategory(), request.getCategory()));
            }
            return;
        }

        try {
             provider.execute(request);
        } catch (Exception ie) {
            if (log.isWarnEnabled()) {
                log.warn(String.format("Gather error, provider: %s, request: %s", provider, request), ie);
            }
            request.setSuccess(false);
            request.setErrorMsg(null == ie.getCause() ? ie.getMessage() : ie.getCause().getMessage());
            if (request.getErrorMsg().contains("wait interrupt")) {
                throw ie;
            }
        }
    }

}
