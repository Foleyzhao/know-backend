package com.cumulus.modules.business.gather.controller;

import java.util.List;

import com.cumulus.modules.business.gather.entity.es.SystemProcessEs;
import com.cumulus.modules.business.gather.service.SystemProcessesEsService;

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
 * 系统进程控制层
 *
 * @author Shijh
 */
@RestController
@RequestMapping("api/es/sp/")
@PreAuthorize("@auth.check('assetWarehouse')")
public class SystemProcessesEsController {

    /**
     * 系统进程接口服务
     */
    @Autowired
    private SystemProcessesEsService systemProcessesService;

    /**
     * 批量添加
     *
     * @param systemProcesses 参数列表
     * @return 结果集
     */
    @PostMapping("saveBatch")
    public ResponseEntity<Object> saveBatch(@RequestBody List<SystemProcessEs> systemProcesses) {
        if (CollectionUtils.isEmpty(systemProcesses)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.systemProcessesService.saveAll(systemProcesses);
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
        this.systemProcessesService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 根据id更新
     *
     * @param systemProcesses 要修改的
     * @return 结果集
     */
    @PostMapping("updateById")
    public ResponseEntity<Object> updateById(@RequestBody SystemProcessEs systemProcesses) {

        this.systemProcessesService.updateById(systemProcesses);
        return new ResponseEntity<>("id = " + systemProcesses.getId() + "更新成功", HttpStatus.NO_CONTENT);
    }

    /**
     * 分页查询
     *
     * @param id       资产id
     * @param pageable 分页条件
     * @return
     */
    @PostMapping("findList")
    public ResponseEntity<Object> findList(@RequestParam(required = false) String id, Pageable pageable) {
        return new ResponseEntity<>(this.systemProcessesService.findListRecent(id, pageable), HttpStatus.OK);
    }
}
