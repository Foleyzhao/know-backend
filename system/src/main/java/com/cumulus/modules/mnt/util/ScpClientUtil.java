package com.cumulus.modules.mnt.util;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

/**
 * SCP客户端工具类
 */
@Slf4j
@AllArgsConstructor
public class ScpClientUtil {

    /**
     * IP地址
     */
    private String ip;

    /**
     * 端口
     */
    private int port;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * IP与客户端的映射列表
     */
    static private final Map<String, ScpClientUtil> instance = Maps.newHashMap();

    /**
     * 获取SCP客户端（不存在则新增）
     *
     * @param ip       IP地址
     * @param port     端口
     * @param username 用户名
     * @param password 密码
     * @return SCP客户端
     */
    static synchronized public ScpClientUtil getInstance(String ip, int port, String username, String password) {
        if (null == instance.get(ip)) {
            instance.put(ip, new ScpClientUtil(ip, port, username, password));
        }
        return instance.get(ip);
    }

    /**
     * 下载文件
     *
     * @param remoteFile           远程待下载文件
     * @param localTargetDirectory 目标存储位置
     */
    public void getFile(String remoteFile, String localTargetDirectory) {
        Connection conn = new Connection(ip, port);
        try {
            conn.connect();
            boolean isAuthenticated = conn.authenticateWithPassword(username, password);
            if (!isAuthenticated) {
                if (log.isWarnEnabled()) {
                    log.warn("authentication failed");
                }
            }
            SCPClient client = new SCPClient(conn);
            client.get(remoteFile, localTargetDirectory);
        } catch (IOException ex) {
            if (log.isWarnEnabled()) {
                log.warn(null, ex);
            }
        } finally {
            conn.close();
        }
    }

    /**
     * 上传文件
     *
     * @param localFile             本地待上传文件
     * @param remoteTargetDirectory 目标文件路径
     */
    public void putFile(String localFile, String remoteTargetDirectory) {
        putFile(localFile, null, remoteTargetDirectory);
    }

    /**
     * 上传文件
     *
     * @param localFile             本地待上传文件
     * @param remoteFileName        目标文件名
     * @param remoteTargetDirectory 目标文件路径
     */
    public void putFile(String localFile, String remoteFileName, String remoteTargetDirectory) {
        putFile(localFile, remoteFileName, remoteTargetDirectory, null);
    }

    /**
     * 上传文件
     *
     * @param localFile             本地待上传文件
     * @param remoteFileName        目标文件名
     * @param remoteTargetDirectory 目标文件路径
     * @param mode                  文件权限模式
     */
    public void putFile(String localFile, String remoteFileName, String remoteTargetDirectory, String mode) {
        Connection conn = new Connection(ip, port);
        try {
            conn.connect();
            boolean isAuthenticated = conn.authenticateWithPassword(username, password);
            if (!isAuthenticated) {
                if (log.isWarnEnabled()) {
                    log.warn("authentication failed");
                }
            }
            SCPClient client = new SCPClient(conn);
            if ((null == mode) || (mode.length() == 0)) {
                mode = "0600";
            }
            if (null == remoteFileName) {
                client.put(localFile, remoteTargetDirectory);
            } else {
                client.put(localFile, remoteFileName, remoteTargetDirectory, mode);
            }
        } catch (IOException ex) {
            if (log.isWarnEnabled()) {
                log.warn(null, ex);
            }
        } finally {
            conn.close();
        }
    }

}
