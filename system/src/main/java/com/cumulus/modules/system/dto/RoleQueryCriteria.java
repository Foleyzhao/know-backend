package com.cumulus.modules.system.dto;

import com.cumulus.annotation.Query;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

/**
 * 系统角色查询传输对象
 */
@Data
public class RoleQueryCriteria {

    /**
     * 模糊查询内容
     */
    @Query(blurry = "name,description")
    private String blurry;

    /**
     * 创建时间
     */
    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;
    
}
