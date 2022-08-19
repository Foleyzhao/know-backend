package com.cumulus.modules.business.gather.controller;

import java.util.List;

import com.cumulus.modules.business.gather.entity.es.PerformanceEs;
import com.cumulus.modules.business.gather.service.PerformanceEsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 性能监控控制层
 *
 * @author Shijh
 */
@RestController
@RequestMapping("/api/es/performance/")
@PreAuthorize("@auth.check('assetWarehouse')")
public class PerformanceMonitoringEsController {

    /**
     * 性能监控服务接口
     */
    @Autowired
    private PerformanceEsService performanceEsService;

    /**
     * 批量添加
     *
     * @param performanceEsList 参数列表
     * @return 结果集
     */
    @PostMapping("saveBatch")
    public ResponseEntity<Object> saveBatch(@RequestBody List<PerformanceEs> performanceEsList) {
        if (CollectionUtils.isEmpty(performanceEsList)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.performanceEsService.saveAll(performanceEsList);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 根据id删除
     *
     * @param id id
     * @return 结果集
     */
    @PostMapping("deleteById")
    public ResponseEntity<Object> deleteById(@RequestParam String id) {
        this.performanceEsService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 根据id更新
     *
     * @param performanceEs 要修改的
     * @return 结果集
     */
    @PostMapping("updateById")
    public ResponseEntity<Object> updateById(@RequestBody PerformanceEs performanceEs) {

        this.performanceEsService.updateById(performanceEs);
        return new ResponseEntity<>("更新成功", HttpStatus.OK);
    }

    /**
     * 分页查询
     *
     * @param id       资产采集id
     * @param pageable 分页条件
     * @return 返回结果
     */
    @PostMapping("findList")
    public ResponseEntity<Object> findList(@RequestParam(required = false) String id, Pageable pageable) {
        return new ResponseEntity<>(this.performanceEsService.findList(id, pageable), HttpStatus.OK);
    }
}

