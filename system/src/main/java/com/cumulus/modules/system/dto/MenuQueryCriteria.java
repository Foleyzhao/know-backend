package com.cumulus.modules.system.dto;

import com.cumulus.annotation.Query;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

/**
 * 系统菜单查询传输对象
 */
@Data
public class MenuQueryCriteria {

    /**
     * 模糊查询内容
     */
    @Query(blurry = "title,component,permission")
    private String blurry;

    /**
     * 创建时间
     */
    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;

    /**
     * 父菜单ID是否为空
     */
    @Query(type = Query.Type.IS_NULL, propName = "pid")
    private Boolean pidIsNull;

    /**
     * 父菜单ID
     */
    @Query
    private Long pid;

}
