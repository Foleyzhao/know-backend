package com.cumulus.modules.business.gather.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 采集记录统计
 *
 * @author zhangxq
 */
@Getter
@Setter
public class GatherRecordCountDto {

    /**
     * 成功
     */
    private Long success;

    /**
     * 失败
     */
    private Long fail;

    /**
     * 部分成功
     */
    private Long partialSuccess;

    /**
     * 全部
     */
    private Long all;

    public GatherRecordCountDto(Long success, Long fail, Long partialSuccess, Long all) {
        this.success = success;
        this.fail = fail;
        this.partialSuccess = partialSuccess;
        this.all = all;
    }
}
