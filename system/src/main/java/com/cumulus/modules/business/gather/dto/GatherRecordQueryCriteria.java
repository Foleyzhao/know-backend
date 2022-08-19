package com.cumulus.modules.business.gather.dto;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.cumulus.annotation.Query;
import lombok.Getter;
import lombok.Setter;

/**
 * 采集记录查询参数实体
 *
 * @author zhangxq
 */
@Getter
@Setter
public class GatherRecordQueryCriteria {

    /**
     * 模糊查询内容
     */
    @Query(blurry = "gatherTaskName,ipList")
    private String blurry;

    /**
     * 任务名称
     */
    @Query(type = Query.Type.INNER_LIKE)
    private String gatherTaskName;

    /**
     * 采集对象
     */
    @Query(type = Query.Type.INNER_LIKE)
    private String ipList;

    /**
     * 采集结果
     */
    @Query(propName = "result", type = Query.Type.IN)
    private Set<Integer> resultIds = new HashSet<>();

    /**
     * 开始时间
     */
    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> startTime;


}
