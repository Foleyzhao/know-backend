package com.cumulus.config.thread;

import com.cumulus.utils.SpringContextHolder;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 自定义线程获取工具类
 */
public class ThreadPoolExecutorUtil {

    /**
     * 获取自定义线程池
     *
     * @return 自定义线程池
     */
    public static ThreadPoolExecutor getCustomizePool() {
        AsyncTaskProperties properties = SpringContextHolder.getBean(AsyncTaskProperties.class);
        return new ThreadPoolExecutor(
                properties.getCorePoolSize(),
                properties.getMaxPoolSize(),
                properties.getKeepAliveSeconds(),
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(properties.getQueueCapacity()),
                new TheadFactoryName()
        );
    }
}
