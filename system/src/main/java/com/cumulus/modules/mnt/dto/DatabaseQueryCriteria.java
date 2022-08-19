package com.cumulus.modules.mnt.dto;

import com.cumulus.annotation.Query;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

/**
 * 数据库查询传输对象
 */
@Data
public class DatabaseQueryCriteria {

    /**
     * 数据库名称
     */
    @Query(type = Query.Type.INNER_LIKE)
    private String name;

    /**
     * 数据库连接地址
     */
    @Query
    private String jdbcUrl;

    /**
     * 创建时间
     */
    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;
}
