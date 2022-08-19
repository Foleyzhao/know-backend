package com.cumulus.modules.mnt.util;

import cn.hutool.core.io.IoUtil;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Vector;

/**
 * 执行shell命令工具类
 */
@Slf4j
public class ExecuteShellUtil {

    /**
     * 标准输出
     */
    private Vector<String> stdout;

    /**
     * 会话
     */
    Session session;

    /**
     * 构造方法
     *
     * @param ipAddress ip地址
     * @param username  用户名
     * @param password  密码
     * @param port      端口
     */
    public ExecuteShellUtil(final String ipAddress, final String username, final String password, int port) {
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(username, ipAddress, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(3000);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        }

    }

    /**
     * 执行指令并返回执行返回码
     *
     * @param command 指令
     * @return 执行返回码
     */
    public int execute(final String command) {
        int returnCode = 0;
        ChannelShell channel = null;
        PrintWriter printWriter = null;
        BufferedReader input = null;
        stdout = new Vector<>();
        try {
            channel = (ChannelShell) session.openChannel("shell");
            channel.connect();
            input = new BufferedReader(new InputStreamReader(channel.getInputStream()));
            printWriter = new PrintWriter(channel.getOutputStream());
            printWriter.println(command);
            printWriter.println("exit");
            printWriter.flush();
            if (log.isInfoEnabled()) {
                log.info("The remote command is: " + command);
            }
            String line;
            while (null != (line = input.readLine())) {
                stdout.add(line);
                if (log.isInfoEnabled()) {
                    log.info(line);
                }
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            return -1;
        } finally {
            IoUtil.close(printWriter);
            IoUtil.close(input);
            if (null != channel) {
                channel.disconnect();
            }
        }
        return returnCode;
    }

    /**
     * 关闭会话
     */
    public void close() {
        if (null != session) {
            session.disconnect();
        }
    }

    /**
     * 执行指令并返回执行回显
     *
     * @param command 指令
     * @return 执行回显
     */
    public String executeForResult(String command) {
        execute(command);
        StringBuilder sb = new StringBuilder();
        for (String str : stdout) {
            sb.append(str);
        }
        return sb.toString();
    }

}
