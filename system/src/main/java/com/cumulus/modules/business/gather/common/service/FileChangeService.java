package com.cumulus.modules.business.gather.common.service;

import java.io.File;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

/**
 * 文件变更监控服务
 *
 * @author zhaoff
 */
public interface FileChangeService {

    /**
     * 增加文件变更监视
     *
     * <p>可以监视一个文件或文件夹，如果监视文件夹，则文件夹内任何文件的变化都会通知
     *
     * @param file 需要监视的文件或文件夹
     */
    void addFileChangeMonitor(File file);

    /**
     * 删除文件变更监视
     *
     * @param file 监视的文件或文件夹
     */
    void removeFileChangeMonitor(File file);

    /**
     * 增加文件变更通知的监听器
     *
     * @param listener 文件变更通知的监听器
     */
    void addFileChangeListener(FileChangeListener listener);

    /**
     * 删除文件变更通知的监听器
     *
     * @param listener 文件变更通知的监听器
     */
    void removeFileChangeListener(FileChangeListener listener);

    /**
     * 文件变更通知的监听器接口
     */
    interface FileChangeListener {

        /**
         * 通知文件发生变更
         *
         * @param kind 变更类型，参考 {@link StandardWatchEventKinds} 中的定义
         * @param file 变更的文件
         */
        void onChange(WatchEvent.Kind<?> kind, File file);
    }

}

