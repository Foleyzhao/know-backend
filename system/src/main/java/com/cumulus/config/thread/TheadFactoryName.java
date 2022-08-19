package com.cumulus.config.thread;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义命名线程工厂
 */
@Component
public class TheadFactoryName implements ThreadFactory {

    /**
     * 创建线程顺序
     */
    private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);

    /**
     * 线程组
     */
    private final ThreadGroup group;

    /**
     * 执行线程顺序
     */
    private final AtomicInteger threadNumber = new AtomicInteger(1);

    /**
     * 线程名前缀
     */
    private final String namePrefix;

    public TheadFactoryName() {
        this("customize-pool");
    }

    private TheadFactoryName(String name) {
        SecurityManager s = System.getSecurityManager();
        group = (null != s) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.namePrefix = name + POOL_NUMBER.getAndIncrement();
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + "-thread-" + threadNumber.getAndIncrement(), 0);
        if (t.isDaemon()) {
            t.setDaemon(false);
        }
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }
    
}
