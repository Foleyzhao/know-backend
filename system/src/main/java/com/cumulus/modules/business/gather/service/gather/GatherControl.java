package com.cumulus.modules.business.gather.service.gather;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 采集控制管理
 *
 * @author zhaoff
 */
@Slf4j
@Component
public class GatherControl {

    /**
     * 信号量维护对象（key 为资产ID）
     */
    private final Map<Long, Semaphore> gatherSems = new HashMap<>();

    /**
     * 上锁
     *
     * @param key     键
     * @param seconds 秒数
     * @return 获取锁是否成功
     * @throws Exception 上锁过程中抛出的异常
     */
    public boolean lock(Long key, Long seconds) throws Exception {
        if (null == key) {
            return false;
        }
        return lockForSingle(key, seconds);
    }

    /**
     * 解锁
     *
     * @param key 键
     */
    public void unlock(Long key) {
        if (null == key) {
            return;
        }
        unlockForSingle(key);
    }

    /**
     * 单机环境下解锁
     *
     * @param key 键
     */
    private void unlockForSingle(Long key) {
        Semaphore lock = gatherSems.get(key);
        if (null != lock) {
            lock.release();
        } else {
            if (log.isWarnEnabled()) {
                log.warn("No lock for key:" + key);
            }
        }
    }

    /**
     * 单机环境下上锁
     *
     * @param key     键
     * @param seconds 秒数
     * @return 上锁是否成功
     * @throws Exception 上锁过程中抛出的异常
     */
    private boolean lockForSingle(Long key, Long seconds) throws Exception {
        Semaphore lock;
        synchronized (gatherSems) {
            lock = gatherSems.get(key);
            if (null == lock) {
                lock = new Semaphore(1);
                gatherSems.put(key, lock);
            }
        }
        if (null == seconds) {
            lock.acquire();
            return true;
        } else {
            return lock.tryAcquire(seconds, TimeUnit.SECONDS);
        }
    }
}
