package com.cumulus.modules.business.gather.provider.impl;

import com.cumulus.modules.business.gather.common.constant.GatherConstants;
import com.cumulus.modules.business.gather.handler.ItemLogHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 主机构建 Provider
 *
 * @author zhaoff
 */
@Slf4j
@Component
public class HostBuildProvider extends AbstractBuildProvider implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext appContext) throws BeansException {
        List<ItemLogHandler> list = new ArrayList<>(BeanFactoryUtils
                .beansOfTypeIncludingAncestors(appContext, ItemLogHandler.class).values());
        list.forEach(h -> {
            if (h.supportAssetTypes().contains(this.getMainCategory())) {
                this.handlerMap.put(h.getEsIndex(), h);
            }
        });
    }

    @PostConstruct
    private void init() {
        getItemKeyMap();
        if (log.isInfoEnabled()) {
            log.info("init finished, handler keys:" + itemKeyMap.keySet());
        }
    }

    /**
     * 返回支持的采集功能分类
     *
     * @return 支持的采集功能分类
     */
    @Override
    public String getCategory() {
        return null;
    }

    /**
     * 返回支持的采集功能主要分类
     *
     * @return 支持的采集功能主要分类
     */
    @Override
    public String getMainCategory() {
        return GatherConstants.TYPE_HOST.toString();
    }

    @Override
    public void getItemKeyMap() {
        if (itemKeyMap.isEmpty()) {
            // 基本信息
            Set<String> basic = new HashSet<>();
            basic.add(GatherConstants.ITEM_KEY_OS_VERSION);
            basic.add(GatherConstants.ITEM_KEY_HOSTNAME);
            basic.add(GatherConstants.ITEM_KEY_KERNEL);
            basic.add(GatherConstants.ITEM_KEY_OS_VENDOR);
            basic.add(GatherConstants.ITEM_KEY_RUNTIME);
            basic.add(GatherConstants.ITEM_KEY_UUID);
            itemKeyMap.put(GatherConstants.ES_INDEX_BASIC_INFO, basic);
            // 硬件信息
            Set<String> hard = new HashSet<>();
            hard.add(GatherConstants.ITEM_KEY_VENDOR);
            hard.add(GatherConstants.ITEM_KEY_SYSTEM_MODEL);
            hard.add(GatherConstants.ITEM_KEY_CPU_MODEL);
            hard.add(GatherConstants.ITEM_KEY_CPU_CORES);
            hard.add(GatherConstants.ITEM_KEY_PHYSICAL_MEMORY);
            hard.add(GatherConstants.ITEM_KEY_DISK_SIZE);
            hard.add(GatherConstants.ITEM_KEY_RUNTIME);
            hard.add(GatherConstants.ITEM_KEY_UUID);
            hard.add(GatherConstants.ITEM_KEY_POWER_NUM);
            hard.add(GatherConstants.ITEM_KEY_POWER_SUPPLY);
            hard.add(GatherConstants.ITEM_KEY_CPU_FREQUENCY);
            hard.add(GatherConstants.ITEM_KEY_MEMORY_NUM);
            itemKeyMap.put(GatherConstants.ES_INDEX_HARDWARE_INFO, hard);
            // 磁盘分区
            Set<String> disk = new HashSet<>();
            disk.add(GatherConstants.ITEM_KEY_DISK_PARTITION);
            itemKeyMap.put(GatherConstants.ES_INDEX_DISK, disk);
            // 帐号信息
            Set<String> account = new HashSet<>();
            account.add(GatherConstants.ITEM_KEY_ACCOUNT_INFO);
            itemKeyMap.put(GatherConstants.ES_INDEX_ACCOUNT, account);
            // 安全配置
            Set<String> security = new HashSet<>();
            security.add(GatherConstants.ITEM_KEY_SECURITY_CFG);
            itemKeyMap.put(GatherConstants.ES_INDEX_SEC_CONFIG, security);
            // 网络配置
            Set<String> network = new HashSet<>();
            network.add(GatherConstants.ITEM_KEY_NET_PARAM);
            itemKeyMap.put(GatherConstants.ES_INDEX_NET_CONFIG, network);
            // 服务
            Set<String> service = new HashSet<>();
            service.add(GatherConstants.ITEM_KEY_SERVICE);
            itemKeyMap.put(GatherConstants.ES_INDEX_SERVICE, service);
            // 路由表
            Set<String> route = new HashSet<>();
            route.add(GatherConstants.ITEM_KEY_ROUTE);
            itemKeyMap.put(GatherConstants.ES_INDEX_ROUTE, route);
            // 环境变量
            Set<String> env = new HashSet<>();
            env.add(GatherConstants.ITEM_KEY_ENVIRONMENT);
            itemKeyMap.put(GatherConstants.ES_INDEX_ENVIRONMENT, env);
            // 端口
            Set<String> netstat = new HashSet<>();
            netstat.add(GatherConstants.ITEM_KEY_NET_STATUS);
            itemKeyMap.put(GatherConstants.ES_INDEX_NETSTAT, netstat);
            // 已装软件
            Set<String> software = new HashSet<>();
            software.add(GatherConstants.ITEM_KEY_SOFTWARE);
            software.add(GatherConstants.ITEM_KEY_ILLEGAL_TOOLS);
            itemKeyMap.put(GatherConstants.ES_INDEX_SW, software);
            // 系统进程
            Set<String> process = new HashSet<>();
            process.add(GatherConstants.ITEM_KEY_PROCESS);
            itemKeyMap.put(GatherConstants.ES_INDEX_PROCESS, process);
            // 性能
            Set<String> performance = new HashSet<>();
            performance.add(GatherConstants.ITEM_KEY_PERFORMANCE);
            itemKeyMap.put(GatherConstants.ES_INDEX_PERFORMANCE, performance);
            // 敏感文件
            Set<String> sensitiveFile = new HashSet<>();
            sensitiveFile.add(GatherConstants.ITEM_KEY_SENSITIVE_FILE);
            itemKeyMap.put(GatherConstants.ES_INDEX_SENSITIVE_FILE, sensitiveFile);
            // 内核更新
            Set<String> kernelUpdate = new HashSet<>();
            kernelUpdate.add(GatherConstants.ITEM_KEY_KERNEL_UPDATE);
            itemKeyMap.put(GatherConstants.ES_INDEX_KERNEL_UPDATE, kernelUpdate);
        }
    }

}
