package com.cumulus.modules.mnt.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 数据源类型枚举类
 */
@Slf4j
@Getter
@AllArgsConstructor
@SuppressWarnings({"unchecked", "all"})
public enum DataTypeEnum {

    /**
     * mysql
     */
    MYSQL("mysql", "mysql", "com.mysql.jdbc.Driver", "`", "`", "'",
            "'"),

    /**
     * oracle
     */
    ORACLE("oracle", "oracle", "oracle.jdbc.driver.OracleDriver", "\"", "\"",
            "\"", "\""),

    /**
     * sql server
     */
    SQLSERVER("sqlserver", "sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver",
            "\"", "\"", "\"", "\""),

    /**
     * h2
     */
    H2("h2", "h2", "org.h2.Driver", "`", "`", "\"", "\""),

    /**
     * phoenix
     */
    PHOENIX("phoenix", "hbase phoenix", "org.apache.phoenix.jdbc.PhoenixDriver", "",
            "", "\"", "\""),

    /**
     * mongo
     */
    MONGODB("mongo", "mongodb", "mongodb.jdbc.MongoDriver", "`", "`",
            "\"", "\""),

    /**
     * sql4es
     */
    ELASTICSEARCH("sql4es", "elasticsearch", "nl.anchormen.sql4es.jdbc.ESDriver", "",
            "", "'", "'"),

    /**
     * presto
     */
    PRESTO("presto", "presto", "com.facebook.presto.jdbc.PrestoDriver", "",
            "", "\"", "\""),

    /**
     * moonbox
     */
    MOONBOX("moonbox", "moonbox", "moonbox.jdbc.MbDriver", "`", "`",
            "`", "`"),

    /**
     * cassandra
     */
    CASSANDRA("cassandra", "cassandra", "com.github.adejanovski.cassandra.jdbc.CassandraDriver",
            "", "", "'", "'"),

    /**
     * click house
     */
    CLICKHOUSE("clickhouse", "clickhouse", "ru.yandex.clickhouse.ClickHouseDriver", "",
            "", "\"", "\""),

    /**
     * kylin
     */
    KYLIN("kylin", "kylin", "org.apache.kylin.jdbc.Driver", "\"", "\"",
            "\"", "\""),

    /**
     * vertica
     */
    VERTICA("vertica", "vertica", "com.vertica.jdbc.Driver", "", "",
            "'", "'"),

    /**
     * sap
     */
    HANA("sap", "sap hana", "com.sap.db.jdbc.Driver", "", "", "'",
            "'"),

    /**
     * impala
     */
    IMPALA("impala", "impala", "com.cloudera.impala.jdbc41.Driver", "", "",
            "'", "'");

    /**
     * 数据库类型
     */
    private String feature;

    /**
     * 描述
     */
    private String desc;

    /**
     * 连接驱动
     */
    private String driver;

    /**
     * 关键字后缀
     */
    private String keywordPrefix;

    /**
     * 关键字前缀
     */
    private String keywordSuffix;

    /**
     * 别名前缀
     */
    private String aliasPrefix;

    /**
     * 别名后缀
     */
    private String aliasSuffix;

    /**
     * jdbc url前缀
     */
    private static final String JDBC_URL_PREFIX = "jdbc:";

    /**
     * 根据JDBC URL获取数据源类型
     *
     * @param jdbcUrl jdbc url
     * @return 数据源类型
     */
    public static DataTypeEnum urlOf(String jdbcUrl) {
        String url = jdbcUrl.toLowerCase().trim();
        for (DataTypeEnum dataTypeEnum : values()) {
            if (url.startsWith(JDBC_URL_PREFIX + dataTypeEnum.feature)) {
                try {
                    Class<?> aClass = Class.forName(dataTypeEnum.getDriver());
                    if (null == aClass) {
                        throw new RuntimeException("Unable to get driver instance for jdbcUrl: " + jdbcUrl);
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Unable to get driver instance: " + jdbcUrl);
                }
                return dataTypeEnum;
            }
        }
        return null;
    }

}
