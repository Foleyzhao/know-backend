package com.cumulus.modules.business.dto;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.cumulus.annotation.Query;
import lombok.Getter;
import lombok.Setter;

/**
 * 确认资产查询参数实体
 *
 * @author zhangxq
 */
@Getter
@Setter
public class AssetConfirmQueryCriteria {

    /**
     * 资产类别 1 主机  2 应用
     */
    @Query(type = Query.Type.EQUAL)
    private Integer assetCategory;

    /**
     * 模糊查询内容
     */
    @Query(blurry = "ip,detectTaskName")
    private String blurry;

    /**
     * ip
     */
    @Query(type = Query.Type.INNER_LIKE)
    private String ip;

    /**
     * 端口
     */
    @Query(type = Query.Type.INNER_LIKE)
    private Integer port;

    /**
     * 资产类型
     */
    @Query(propName = "assetType", type = Query.Type.IN)
    private Set<Long> assetTypeIds = new HashSet<>();

    /**
     * 资产sys类型
     */
    @Query(propName = "assetSysType", type = Query.Type.IN)
    private Set<Long> assetSysTypeIds = new HashSet<>();

    /**
     * 任务名称
     */
    @Query(type = Query.Type.INNER_LIKE)
    private String detectTaskName;

    /**
     * 创建时间
     */
    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;

}
