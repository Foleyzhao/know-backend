package com.cumulus.modules.business.gather.controller;

import javax.annotation.Resource;

import com.cumulus.modules.business.gather.service.GatherRecordService;
import com.cumulus.modules.business.gather.vo.GatherRecordVo;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 采集结果控制层
 *
 * @author zhangxq
 */
@RestController
@RequestMapping("/api/gatherRecord")
@PreAuthorize("@auth.check('dataGather')")
public class GatherRecordController {

    /**
     * 采集任务服务接口
     */
    @Resource
    private GatherRecordService gatherRecordService;

    /**
     * 分页查询发现任务
     *
     * @param gatherRecordVo 查询参数
     * @param pageable       分页参数
     * @return 发现任务列表
     */
    @GetMapping("/query")
    public ResponseEntity<Object> queryAll(GatherRecordVo gatherRecordVo, Pageable pageable) {
        return new ResponseEntity<>(gatherRecordService.queryAll(gatherRecordVo, pageable), HttpStatus.OK);
    }


    /**
     * 任务记录统计
     *
     * @return 统计对象
     */
    @GetMapping("/count")
    public ResponseEntity<Object> count() {
        return new ResponseEntity<>(gatherRecordService.countRecord(), HttpStatus.OK);
    }
}
