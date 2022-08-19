package com.cumulus.modules.business.gather.controller;

import com.cumulus.modules.business.gather.service.MiddlewareEsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 中间件控制层
 *
 * @author Shijh
 */
@RestController
@RequestMapping("/api/es/middleware")
public class MiddlewareEsController {

    /**
     * 中间件接口服务
     */
    @Autowired
    private MiddlewareEsService middlewareEsService;

    /**
     * 分页查询
     *
     * @param id       资产id
     * @param pageable 分页条件
     * @return 返回分页信息
     */
    @PostMapping("findList")
    public ResponseEntity<Object> findList(@RequestParam(required = false) String id, Pageable pageable) {
        return new ResponseEntity<>(this.middlewareEsService.findListRecent(id, pageable), HttpStatus.OK);
    }
}
