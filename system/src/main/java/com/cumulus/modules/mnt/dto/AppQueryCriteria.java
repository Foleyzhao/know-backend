package com.cumulus.modules.mnt.dto;

import com.cumulus.annotation.Query;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

/**
 * 应用查询传输对象
 */
@Data
public class AppQueryCriteria {

    /**
     * 名称
     */
    @Query(type = Query.Type.INNER_LIKE)
    private String name;

    /**
     * 创建时间
     */
    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;
}
