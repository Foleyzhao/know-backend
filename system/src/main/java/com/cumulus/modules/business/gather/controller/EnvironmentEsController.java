package com.cumulus.modules.business.gather.controller;

import java.util.List;

import com.cumulus.modules.business.gather.entity.es.EnvironmentEs;
import com.cumulus.modules.business.gather.service.EnvironmentEsService;


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
 * 环境变量控制层
 *
 * @author Shijh
 */
@RestController
@RequestMapping("/api/es/environment/")
@PreAuthorize("@auth.check('assetWarehouse')")
public class EnvironmentEsController {

    /**
     * 环境变量服务接口
     */
    @Autowired
    private EnvironmentEsService environmentEsService;

    /**
     * 批量添加
     *
     * @param environmentEsList 参数列表
     * @return 结果集
     */
    @PostMapping("saveBatch")
    public ResponseEntity<Object> saveBatch(@RequestBody List<EnvironmentEs> environmentEsList) {
        if (CollectionUtils.isEmpty(environmentEsList)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.environmentEsService.saveAll(environmentEsList);
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
        this.environmentEsService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 根据id更新
     *
     * @param environmentEs 要修改的
     * @return 结果集
     */
    @PostMapping("updateById")
    public ResponseEntity<Object> updateById(@RequestBody EnvironmentEs environmentEs) {
        this.environmentEsService.updateById(environmentEs);
        return new ResponseEntity<>("id = " + environmentEs.getId() + "更新成功", HttpStatus.NO_CONTENT);
    }

    /**
     * 分页查询
     *
     * @param pageable 分页条件
     * @return
     */
    @PostMapping("findList")
    public ResponseEntity<Object> findList(@RequestParam(required = false) String id, Pageable pageable) {
        return new ResponseEntity<>(this.environmentEsService.findListRecent(id, pageable), HttpStatus.OK);
    }
}
