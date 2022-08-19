package com.cumulus.modules.business.gather.dto;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.cumulus.annotation.Query;
import lombok.Getter;
import lombok.Setter;

/**
 * 采集任务查询参数实体
 *
 * @author zhangxq
 */
@Getter
@Setter
public class GatherTaskQueryCriteria {

    /**
     * id
     */
    @Query(type = Query.Type.EQUAL)
    private Long id;

    /**
     * 模糊查询内容
     */
    @Query(blurry = "name")
    private String blurry;

    /**
     * 任务名称
     */
    @Query(propName = "name",type = Query.Type.INNER_LIKE)
    private String gatherTaskName;

    /**
     * 任务状态
     */
    @Query(propName = "status", type = Query.Type.IN)
    private Set<Integer> taskStatus = new HashSet<>();

    /**
     * ip范围 目标网段
     */
    @Query(propName = "ip", type = Query.Type.INNER_LIKE, joinName = "assetList")
    private String ipList;

    /**
     * 创建时间
     */
    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;

}
