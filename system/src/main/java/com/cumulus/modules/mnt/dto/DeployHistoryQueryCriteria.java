package com.cumulus.modules.mnt.dto;

import com.cumulus.annotation.Query;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

/**
 * 部署历史查询传输对象
 */
@Data
public class DeployHistoryQueryCriteria {

    /**
     * 模糊查询字段
     */
    @Query(blurry = "appName,ip,deployUser")
    private String blurry;

    /**
     * 部署ID
     */
    @Query
    private Long deployId;

    /**
     * 部署时间
     */
    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> deployDate;
}
