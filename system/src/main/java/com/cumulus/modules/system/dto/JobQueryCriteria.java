package com.cumulus.modules.system.dto;

import com.cumulus.annotation.Query;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

/**
 * 系统岗位查询传输对象
 */
@Data
@NoArgsConstructor
public class JobQueryCriteria {

    /**
     * 岗位名称
     */
    @Query(type = Query.Type.INNER_LIKE)
    private String name;

    /**
     * 是否启用
     */
    @Query
    private Boolean enabled;

    /**
     * 创建时间
     */
    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;

}