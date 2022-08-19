package com.cumulus.modules.business.gather.entity.es;

import org.springframework.data.annotation.Id;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecurityConfig {

    /**
     * 记录id
     */
    @Id
    private String id;

    /**
     * 所属资产的id
     */
    private String assetId;

    /**
     * 采集项的key
     */
    private String itemkey;

    /**
     * 变量名
     */
    private String variable;

    /**
     * 采集项名
     */
    private String itemName;

    /**
     * 采集分组
     */
    private String groupName;

    /**
     * 系统类型
     */
    private String systype;

    /**
     * 系统类型ID
     */
    private Long systypeId;

    /**
     * 风险类型
     */
    private List<Long> riskType;

    /**
     * 风险等级
     */
    private Integer level;

    /**
     * 详细信息
     */
    private Map<String, Object> detail = new HashMap<>();

    /**
     * 采集信息，为方便查询和获取信息，对一些信息进行提取
     */
    private Map<String, Object> gatherInfo = new HashMap<>();

    /**
     * 采集时间
     */
    private long utime;

}
