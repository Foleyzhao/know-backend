package com.cumulus.config.thread;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 线程池属性配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "task.pool")
public class AsyncTaskProperties {

    /**
     * 核心线程池大小
     */
    private int corePoolSize;

    /**
     * 最大线程数
     */
    private int maxPoolSize;

    /**
     * 活跃时间
     */
    private int keepAliveSeconds;

    /**
     * 队列容量
     */
    private int queueCapacity;
}
