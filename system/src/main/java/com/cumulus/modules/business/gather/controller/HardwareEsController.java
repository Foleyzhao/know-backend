package com.cumulus.modules.business.gather.controller;

import com.cumulus.modules.business.gather.entity.es.HardwareEs;
import com.cumulus.modules.business.gather.service.HardwareEsService;

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

import java.util.List;

/**
 * 硬件信息控制层
 *
 * @author shijh
 */
@RestController
@RequestMapping("/api/es/hardware/")
@PreAuthorize("@auth.check('assetWarehouse')")
public class HardwareEsController {

    /**
     * 硬件信息服务接口
     */
    @Autowired
    private HardwareEsService hardwareEsService;

    /**
     * 批量添加
     *
     * @param hardwareEs 参数列表
     * @return 结果集
     */
    @PostMapping("saveBatch")
    public ResponseEntity<Object> saveBatch(@RequestBody List<HardwareEs> hardwareEs) {
        if (CollectionUtils.isEmpty(hardwareEs)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.hardwareEsService.saveAll(hardwareEs);
        return new ResponseEntity<>("新增成功", HttpStatus.CREATED);
    }

    /**
     * 根据id删除
     *
     * @param id id
     * @return 结果集
     */
    @PostMapping("deleteById")
    public ResponseEntity<Object> deleteById(@RequestParam String id) {
        this.hardwareEsService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 根据id更新
     *
     * @param hardwareEs 要修改的
     * @return 结果集
     */
    @PostMapping("updateById")
    public ResponseEntity<Object> updateById(@RequestBody HardwareEs hardwareEs) {

        this.hardwareEsService.updateById(hardwareEs);
        return new ResponseEntity<>("id = " + hardwareEs.getId() + "更新成功", HttpStatus.NO_CONTENT);
    }

    /**
     * 分页查询
     *
     * @param id       资产采集id
     * @param pageable 分页条件
     * @return 返回分页结果
     */
    @PostMapping("findList")
    public ResponseEntity<Object> findList(@RequestParam(required = false) String id, Pageable pageable) {
        return new ResponseEntity<>(this.hardwareEsService.findListRecent(id, pageable), HttpStatus.OK);
    }
}
