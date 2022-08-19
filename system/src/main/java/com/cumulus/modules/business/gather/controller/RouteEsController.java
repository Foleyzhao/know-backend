package com.cumulus.modules.business.gather.controller;

import java.util.List;

import com.cumulus.modules.business.gather.entity.es.RouteEs;
import com.cumulus.modules.business.gather.service.RouteEsService;

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
 * 路由控制层
 *
 * @author Shijh
 */
@RestController
@RequestMapping("api/es/route/")
@PreAuthorize("@auth.check('assetWarehouse')")
public class RouteEsController {

    /**
     * 路由接口服务
     */
    @Autowired
    private RouteEsService routeService;

    /**
     * 批量添加
     *
     * @param routes 参数列表
     * @return 结果集
     */
    @PostMapping("saveBatch")
    public ResponseEntity<Object> saveBatch(@RequestBody List<RouteEs> routes) {
        if (CollectionUtils.isEmpty(routes)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.routeService.saveAll(routes);
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
        this.routeService.deleteById(id);
        return new ResponseEntity<>("删除成功", HttpStatus.OK);
    }

    /**
     * 根据id更新
     *
     * @param route 要修改的
     * @return 结果集
     */
    @PostMapping("updateById")
    public ResponseEntity<Object> updateById(@RequestBody RouteEs route) {

        this.routeService.updateById(route);
        return new ResponseEntity<>("更新成功", HttpStatus.NO_CONTENT);
    }

    /**
     * 分页查询
     *
     * @param id       资产表的id
     * @param pageable 分页条件
     * @return 返回分页结果
     */
    @PostMapping("findList")
    public ResponseEntity<Object> findList(@RequestParam(required = false) String id, Pageable pageable) {
        return new ResponseEntity<>(this.routeService.findListRecent(id, pageable), HttpStatus.OK);
    }
}

