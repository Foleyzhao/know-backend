package com.cumulus.utils;

import java.io.Closeable;

/**
 * 关闭连接工具类
 */
public class CloseUtils {

    /**
     * 关闭连接
     *
     * @param closeable 待关闭连接对象
     */
    public static void close(Closeable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (Exception e) {
                // 静默关闭
            }
        }
    }

    /**
     * 关闭连接
     *
     * @param closeable 待关闭连接对象
     */
    public static void close(AutoCloseable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (Exception e) {
                // 静默关闭
            }
        }
    }
}
