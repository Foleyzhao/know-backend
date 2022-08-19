package com.cumulus.dto;

import com.cumulus.annotation.Query;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

/**
 * 本地存储查询传输对象
 *
 * @author zhaoff
 */
@Data
public class LocalStorageQueryCriteria {

    /**
     * 模糊查询字段
     */
    @Query(blurry = "name,suffix,type,createBy,size")
    private String blurry;

    /**
     * 创建时间
     */
    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;
}
