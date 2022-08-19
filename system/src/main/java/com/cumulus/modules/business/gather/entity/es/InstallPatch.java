package com.cumulus.modules.business.gather.entity.es;

import org.springframework.data.annotation.Id;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstallPatch {

    /**
     * 资产ID
     */
    @Id
    private String id;

    /**
     * 所属资产ID
     */
    private String assetId;

    /**
     * 资产的数据库id
     */
    private Long devId;

    /**
     * 补丁名称
     */
    private String name;

    /**
     * 安装日期
     */
    private Long installDate;

    /**
     * 采集时间
     */
    private Long utime;

    /**
     * 风险项ID
     */
    private List<Long> riskType;

    /**
     * 风险等级
     */
    private Integer level;

    /**
     * 是否是最新采集的数据
     */
    private boolean latest;

    /**
     * 补丁的详细信息
     */
    private Map<String, Object> detail = new HashMap<>();

    /**
     * 采集信息，为方便查询和获取信息，对一些信息进行提取
     */
    private Map<String, Object> gatherInfo = new HashMap<>();
}
