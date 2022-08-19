package com.cumulus.dto;

import com.cumulus.annotation.Query;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

/**
 * 操作日志查询对象
 *
 * @author shenjc
 */
@Data
public class LogQueryCriteria {

    public static final int CREATE_TIME_SIZE = 2;

    /**
     * 模糊查询字段
     */
    @Query(blurry = "description,requestIp,username")
    private String blurry;

    /**
     * 模糊查询字段 用户名
     */
    @Query(type = Query.Type.INNER_LIKE)
    private String username;

    /**
     * 模糊查询字段 ip
     */
    @Query(type = Query.Type.INNER_LIKE)
    private String requestIp;

    /**
     * 日志类型
     */
    @Query
    private String logType;

    /**
     * ID列表
     */
    @Query(propName = "id", type = Query.Type.IN)
    private List<Long> idList;


    /**
     * 日志类型不等于
     */
    @Query(propName = "logType", type = Query.Type.NOT_EQUAL)
    private String logTypeNe;

    /**
     * 创建日期
     */
    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;

}
