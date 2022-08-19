package com.cumulus.modules.business.detect.dto;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.cumulus.annotation.Query;
import lombok.Getter;
import lombok.Setter;

/**
 * 发现任务查询参数实体
 *
 * @author zhangxq
 */
@Getter
@Setter
public class DetectTaskQueryCriteria {

    /**
     * id
     */
    @Query(type = Query.Type.EQUAL)
    private Long id;

    /**
     * 模糊查询内容
     */
    @Query(blurry = "detectTaskName")
    private String blurry;

    /**
     * 任务名称
     */
    @Query(type = Query.Type.INNER_LIKE)
    private String detectTaskName;

    /**
     * 任务状态
     */
    @Query(propName = "taskStatus", type = Query.Type.IN)
    private Set<Integer> taskStatus = new HashSet<>();

    /**
     * 任务类型
     */
    @Query(propName = "taskType", type = Query.Type.IN)
    private Set<Integer> taskTypes = new HashSet<>();

    /**
     * ip范围 目标网段
     */
    @Query(type = Query.Type.INNER_LIKE)
    private String ipList;

    /**
     * 创建时间
     */
    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;

}
