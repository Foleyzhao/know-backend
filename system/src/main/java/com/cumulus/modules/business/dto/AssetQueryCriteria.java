package com.cumulus.modules.business.dto;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.cumulus.annotation.Query;
import lombok.Getter;
import lombok.Setter;

/**
 * 资产查询传输对象
 *
 * @author zhangxq
 */
@Getter
@Setter
public class AssetQueryCriteria {

    /**
     * 模糊查询内容
     */
    @Query(blurry = "ip,name,port")
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
     * 资产名称
     */
    @Query(type = Query.Type.INNER_LIKE)
    private String name;

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
     * 资产标签
     */
    @Query(propName = "id", type = Query.Type.IN, joinName = "assetTags")
    private Set<Long> assetTagIds = new HashSet<>();

    /**
     * 资产部门
     */
    @Query(propName = "dept", type = Query.Type.IN)
    private Set<Long> deptIds = new HashSet<>();

    /**
     * 协议
     */
    @Query(propName = "protocol", type = Query.Type.IN)
    private Set<String> protocols = new HashSet<>();

    /**
     * 资产状态
     */
    @Query(propName = "assetStatus", type = Query.Type.IN)
    private Set<Integer> assetStatus = new HashSet<>();

    /**
     * 资产状态
     */
    @Query(propName = "riskLevel", type = Query.Type.IN)
    private Set<Integer> riskLevels = new HashSet<>();

    /**
     * web地址
     */
    @Query(type = Query.Type.INNER_LIKE)
    private String webAddress;

    /**
     * 指纹信息
     */
    @Query(type = Query.Type.INNER_LIKE)
    private String fingerprint;

    /**
     * 创建时间
     */
    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;

    /**
     * 最小更新时间更新时间
     */
    @Query(propName = "updateTime", type = Query.Type.GREATER_THAN)
    private Date minUpdateTime;

    /**
     * 最小更新时间更新时间  Timestamp 会出问题
     */
    @Query(propName = "updateTime", type = Query.Type.LESS_THAN)
    private Date maxUpdateTime;

    /**
     * 资产类别（主机，应用）
     */
    @Query(type = Query.Type.EQUAL)
    private Integer assetCategory;
}
