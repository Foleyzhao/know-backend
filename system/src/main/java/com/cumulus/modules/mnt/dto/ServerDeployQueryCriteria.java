package com.cumulus.modules.mnt.dto;

import com.cumulus.annotation.Query;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

/**
 * 服务器查询传输对象
 */
@Data
public class ServerDeployQueryCriteria {

    /**
     * 模糊查询字段
     */
    @Query(blurry = "name,ip,account")
    private String blurry;

    /**
     * 创建时间
     */
    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;

}
