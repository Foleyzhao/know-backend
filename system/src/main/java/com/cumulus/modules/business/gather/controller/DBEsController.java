package com.cumulus.modules.business.gather.controller;

import com.cumulus.modules.business.gather.service.DBEsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据库控制层
 *
 * @author Shijh
 */
@RestController
@RequestMapping("api/es/db/")
public class DBEsController {

    /**
     * 数据库接口服务
     */
    @Autowired
    private DBEsService dbEsService;

    /**
     * 分页查询
     *
     * @param id       资产采集id
     * @param pageable 分页条件
     * @return 结果集
     */
    @PostMapping("findList")
    public ResponseEntity<Object> findList(@RequestParam(required = false) String id, Pageable pageable) {
        return new ResponseEntity<>(this.dbEsService.findListRecent(id, pageable), HttpStatus.OK);
    }

}
