package com.cumulus.modules.business.gather.common.service.impl;

import com.cumulus.modules.business.gather.common.service.FileChangeService;
import com.cumulus.modules.business.gather.common.utils.CommUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashSet;
import java.util.Set;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * 文件变更监控服务实现
 *
 * @author zhaoff
 */
@Slf4j
@Component(value = "fileChangeService")
public class FileChangeServiceImpl implements FileChangeService {

    /**
     * 需要监视的文件
     */
    private final Set<File> filesToMonitor = new HashSet<>();

    /**
     * 所有文件变更通知的监听器
     */
    private final Set<FileChangeListener> fileChangeListeners = new HashSet<>();

    /**
     * 文件变更检测的监视器
     */
    private FileChangeWatcher watcher = null;

    /**
     * 平台配置文件存放的目录
     */
    public static final String gatherConfigDir = CommUtils.getDataDir().concat(File.separator).concat("gather")
            .concat(File.separator);

    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        synchronized (filesToMonitor) {
            File configFile = new File(gatherConfigDir);
            if (configFile.exists()) {
                filesToMonitor.add(configFile);
            } else {
                if (log.isErrorEnabled()) {
                    log.error("Platform config dir does not exists:" + configFile.getAbsolutePath());
                }
            }
            watcher = new FileChangeWatcher();
            watcher.start();
        }
    }

    /**
     * 销毁方法，回收资源
     */
    @PreDestroy
    public void destroy() {
        synchronized (filesToMonitor) {
            if (null != watcher) {
                watcher.shutdown();
                watcher = null;
            }
        }
    }

    @Override
    public void addFileChangeMonitor(File file) {
        synchronized (filesToMonitor) {
            if (!filesToMonitor.contains(file)) {
                boolean start = false;
                if (null != watcher && watcher.isAlive()) {
                    start = true;
                    watcher.interrupt();
                }
                filesToMonitor.add(file);
                if (start) {
                    watcher = new FileChangeWatcher();
                    watcher.start();
                }
            }
        }
    }

    @Override
    public void removeFileChangeMonitor(File file) {
        synchronized (filesToMonitor) {
            if (filesToMonitor.contains(file)) {
                boolean start = false;
                if (null != watcher && watcher.isAlive()) {
                    start = true;
                    watcher.interrupt();
                }
                filesToMonitor.remove(file);
                if (start) {
                    watcher = new FileChangeWatcher();
                    watcher.start();
                }
            }
        }
    }

    @Override
    public void addFileChangeListener(FileChangeListener listener) {
        synchronized (fileChangeListeners) {
            fileChangeListeners.add(listener);
        }
    }

    @Override
    public void removeFileChangeListener(FileChangeListener listener) {
        synchronized (fileChangeListeners) {
            fileChangeListeners.remove(listener);
        }
    }

    /**
     * 通知变更
     *
     * @param kind 类型
     * @param path 路径
     */
    public void notifyChange(WatchEvent.Kind<?> kind, String path) {
        // 找到变化的文件
        File fileChanged = null;
        long lastModified = 0;
        synchronized (filesToMonitor) {
            for (File file : filesToMonitor) {
                if (file.getName().equals(path) && file.lastModified() > lastModified) {
                    fileChanged = file;
                    lastModified = file.lastModified();
                } else if (file.isDirectory()) {
                    File tempFile = new File(file, path);
                    if (tempFile.exists() && tempFile.lastModified() > lastModified) {
                        fileChanged = tempFile;
                        lastModified = tempFile.lastModified();
                    }
                }
            }
        }

        if (null == fileChanged) {
            // 这请求监控一个文件，但实际上是监控该文件的父目录，当父目录中其他文件发生变化时会发生此情况
            if (log.isDebugEnabled()) {
                log.debug("File changed but do not care: " + path);
            }
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Send file change event: " + fileChanged);
        }
        synchronized (fileChangeListeners) {
            for (FileChangeListener listener : fileChangeListeners) {
                listener.onChange(kind, fileChanged);
            }
        }
    }

    /**
     * 监视文件变更的类
     */
    class FileChangeWatcher extends Thread {

        /**
         * 运行标识
         */
        private boolean running;

        /**
         * 构造方法
         */
        public FileChangeWatcher() {
            super();
            setName("FILE_CHANGE_WATCHER");
            setDaemon(true);
            running = true;
        }

        /**
         * 关闭线程
         */
        public void shutdown() {
            running = false;
            interrupt();
        }

        @Override
        public void run() {
            try {
                WatchService watcher = FileSystems.getDefault().newWatchService();
                WatchEvent.Kind<?>[] events = {ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE};
                for (File file : filesToMonitor) {
                    if (!file.exists()) {
                        if (log.isDebugEnabled()) {
                            log.warn("File not exist: " + file);
                        }
                        continue;
                    } else if (file.isFile()) {
                        file = file.getParentFile();
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Start monitoring: " + file);
                    }
                    file.toPath().register(watcher, events);
                }
                while (running) {
                    WatchKey watchKey = watcher.take();
                    for (WatchEvent<?> event : watchKey.pollEvents()) {
                        String path = ((Path) event.context()).toString();
                        notifyChange(event.kind(), path);
                    }
                    watchKey.reset();
                }
            } catch (Exception ie) {
                if (log.isErrorEnabled()) {
                    log.error("Start monitoring exception: ", ie);
                }
            }
        }
    }
}
