package com.cumulus.modules.system.dto;

import com.cumulus.annotation.DataPermission;
import com.cumulus.annotation.Query;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

/**
 * 系统部门查询传输对象
 */
@Data
@DataPermission(fieldName = "id")
public class DeptQueryCriteria {

    /**
     * 部门名称
     */
    @Query(type = Query.Type.INNER_LIKE)
    private String name;

    /**
     * 是否启用
     */
    @Query
    private Boolean enabled;

    /**
     * 上级部门ID
     */
    @Query
    private Long pid;

    /**
     * 上级部门ID是否为空
     */
    @Query(type = Query.Type.IS_NULL, propName = "pid")
    private Boolean pidIsNull;

    /**
     * 创建时间
     */
    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;

}