package com.cumulus.modules.business.gather.service;

import com.cumulus.modules.business.gather.vo.GatherRecordVo;

import org.springframework.data.domain.Pageable;

/**
 * 采集记录服务接口
 *
 * @author zhangxq
 */
public interface GatherRecordService {

    /**
     * 查询采集记录
     *
     * @param gatherRecordVo 查询参数
     * @param pageable 分页参数
     * @return 采集记录列表
     */
    Object queryAll(GatherRecordVo gatherRecordVo, Pageable pageable);

    /**
     * 统计采集记录
     *
     * @return 统计采集记录传输对象
     */
    Object countRecord();
}
