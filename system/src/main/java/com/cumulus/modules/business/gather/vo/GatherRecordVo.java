package com.cumulus.modules.business.gather.vo;

import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * 采集记录Vo
 *
 * @author Shijh
 */
@Data
public class GatherRecordVo {

    /**
     * 名称
     */
    private String name;

    /**
     * 名称
     */
    private List<Integer> result;

    /**
     * ip
     */
    private String assetIp;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 排序字段
     */
    private String field;

    /**
     * 排序规则，asc升序，desc降序
     */
    private String order;

    /**
     * 全部
     */
    private Integer all;

    /**
     * 成功
     */
    private Integer success;

    /**
     * 失败
     */
    private Integer fail;

    /**
     * 部分成功
     */
    private Integer partialSuccess;

    /**
     * 名称/IP 模糊查询
     */
    private String blurry;

    /**
     * 采集
     */
    private Integer gatherObj;

}
