package com.cumulus.modules.mnt.util;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.util.StringUtils;
import com.cumulus.utils.CloseUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * SQL工具类
 */
@Slf4j
public class SqlUtils {

    /**
     * 获取数据源
     *
     * @param jdbcUrl  jdbc url
     * @param userName 用户名
     * @param password 密码
     * @return 数据源
     */
    private static DataSource getDataSource(String jdbcUrl, String userName, String password) {
        DruidDataSource druidDataSource = new DruidDataSource();
        String className;
        try {
            className = DriverManager.getDriver(jdbcUrl.trim()).getClass().getName();
        } catch (SQLException e) {
            throw new RuntimeException("Get class name error: =" + jdbcUrl);
        }
        if (StringUtils.isEmpty(className)) {
            DataTypeEnum dataTypeEnum = DataTypeEnum.urlOf(jdbcUrl);
            if (null == dataTypeEnum) {
                throw new RuntimeException("Not supported data type: jdbcUrl=" + jdbcUrl);
            }
            druidDataSource.setDriverClassName(dataTypeEnum.getDriver());
        } else {
            druidDataSource.setDriverClassName(className);
        }
        druidDataSource.setUrl(jdbcUrl);
        druidDataSource.setUsername(userName);
        druidDataSource.setPassword(password);
        // 配置获取连接等待超时的时间
        druidDataSource.setMaxWait(3000);
        // 配置初始化大小、最小、最大
        druidDataSource.setInitialSize(1);
        druidDataSource.setMinIdle(1);
        druidDataSource.setMaxActive(1);
        // 如果链接出现异常则直接判定为失败而不是一直重试
        druidDataSource.setBreakAfterAcquireFailure(true);
        try {
            druidDataSource.init();
        } catch (SQLException e) {
            if (log.isErrorEnabled()) {
                log.error("Exception during pool initialization", e);
            }
            throw new RuntimeException(e.getMessage());
        }
        return druidDataSource;
    }

    /**
     * 获取数据库连接
     *
     * @param jdbcUrl  jdbc url
     * @param userName 用户名
     * @param password 密码
     * @return 数据库连接
     */
    private static Connection getConnection(String jdbcUrl, String userName, String password) {
        DataSource dataSource = getDataSource(jdbcUrl, userName, password);
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
        } catch (Exception ignored) {
        }
        try {
            int timeOut = 5;
            if (null == connection || connection.isClosed() || !connection.isValid(timeOut)) {
                if (log.isInfoEnabled()) {
                    log.info("connection is closed or invalid, retry get connection!");
                }
                connection = dataSource.getConnection();
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("create connection error, jdbcUrl: {}", jdbcUrl);
            }
            throw new RuntimeException("create connection error, jdbcUrl: " + jdbcUrl);
        } finally {
            CloseUtils.close(connection);
        }
        return connection;
    }

    /**
     * 释放数据库连接
     *
     * @param connection 数据库连接
     */
    private static void releaseConnection(Connection connection) {
        if (null != connection) {
            try {
                connection.close();
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error("connection close error：" + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 测试连接
     *
     * @param jdbcUrl  jdbc url
     * @param userName 用户名
     * @param password 密码
     * @return 测试结果
     */
    public static boolean testConnection(String jdbcUrl, String userName, String password) {
        Connection connection = null;
        try {
            connection = getConnection(jdbcUrl, userName, password);
            if (null != connection) {
                return true;
            }
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info("Get connection failed:" + e.getMessage());
            }
        } finally {
            releaseConnection(connection);
        }
        return false;
    }

    /**
     * 执行sql文件
     *
     * @param jdbcUrl  jdbc url
     * @param userName 用户名
     * @param password 密码
     * @param sqlFile  sql文件
     * @return 执行结果
     */
    public static String executeFile(String jdbcUrl, String userName, String password, File sqlFile) {
        Connection connection = getConnection(jdbcUrl, userName, password);
        try {
            batchExecute(connection, readSqlList(sqlFile));
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("sql脚本执行发生异常:{}", e.getMessage());
            }
            return e.getMessage();
        } finally {
            releaseConnection(connection);
        }
        return "success";
    }


    /**
     * 批量执行sql
     *
     * @param connection 数据库连接
     * @param sqlList    sql语句列表
     */
    public static void batchExecute(Connection connection, List<String> sqlList) {
        Statement st = null;
        try {
            st = connection.createStatement();
            for (String sql : sqlList) {
                if (sql.endsWith(";")) {
                    sql = sql.substring(0, sql.length() - 1);
                }
                st.addBatch(sql);
            }
            st.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            CloseUtils.close(st);
        }
    }

    /**
     * 解析sql文件
     *
     * @param sqlFile sql文件
     * @return sql语句列表
     * @throws Exception 异常
     */
    private static List<String> readSqlList(File sqlFile) throws Exception {
        List<String> sqlList = Lists.newArrayList();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(sqlFile), StandardCharsets.UTF_8))) {
            String tmp;
            while (null != (tmp = reader.readLine())) {
                if (log.isInfoEnabled()) {
                    log.info("line:{}", tmp);
                }
                if (tmp.endsWith(";")) {
                    sb.append(tmp);
                    sqlList.add(sb.toString());
                    sb.delete(0, sb.length());
                } else {
                    sb.append(tmp);
                }
            }
            if (!"".endsWith(sb.toString().trim())) {
                sqlList.add(sb.toString());
            }
        }
        return sqlList;
    }

}
