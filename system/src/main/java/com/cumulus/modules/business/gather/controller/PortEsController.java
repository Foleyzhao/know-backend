package com.cumulus.modules.business.gather.controller;

import java.util.List;

import com.cumulus.modules.business.gather.entity.es.PortEs;
import com.cumulus.modules.business.gather.service.PortEsService;

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
 * 端口控制层
 *
 * @author Shijh
 */
@RestController
@RequestMapping("api/es/port/")
@PreAuthorize("@auth.check('assetWarehouse')")
public class PortEsController {

    /**
     * 端口接口服务
     */
    @Autowired
    private PortEsService portService;

    /**
     * 批量添加
     *
     * @param ports 参数列表
     * @return 结果集
     */
    @PostMapping("saveBatch")
    public ResponseEntity<Object> saveBatch(@RequestBody List<PortEs> ports) {
        if (CollectionUtils.isEmpty(ports)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.portService.saveAll(ports);
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
        this.portService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 根据id更新
     *
     * @param port 要修改的
     * @return 结果集
     */
    @PostMapping("updateById")
    public ResponseEntity<Object> updateById(@RequestBody PortEs port) {

        this.portService.updateById(port);
        return new ResponseEntity<>("id = " + port.getId() + "更新成功", HttpStatus.NO_CONTENT);
    }

    /**
     * 分页查询
     *
     * @param id       资产采集id
     * @param pageable 分页条件
     * @return 结果集
     */
    @PostMapping("findList")
    public ResponseEntity<Object> findList(@RequestParam(required = false) String id, Pageable pageable) {
        return new ResponseEntity<>(this.portService.findListRecent(id, pageable), HttpStatus.OK);
    }
}

