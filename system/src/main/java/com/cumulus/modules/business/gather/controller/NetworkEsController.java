package com.cumulus.modules.business.gather.controller;

import java.util.List;

import com.cumulus.modules.business.gather.entity.es.NetworkEs;
import com.cumulus.modules.business.gather.service.NetworkEsService;

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
 * 网络配置控制层
 *
 * @author Shijh
 */
@RestController
@RequestMapping("/api/es/network/")
@PreAuthorize("@auth.check('assetWarehouse')")
public class NetworkEsController {

    /**
     * 网络配置接口服务
     */
    @Autowired
    private NetworkEsService networkEsService;

    /**
     * 批量添加
     *
     * @param networkEsList 参数列表
     * @return 结果集
     */
    @PostMapping("saveBatch")
    public ResponseEntity<Object> saveBatch(@RequestBody List<NetworkEs> networkEsList) {
        if (CollectionUtils.isEmpty(networkEsList)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.networkEsService.saveAll(networkEsList);
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
        this.networkEsService.deleteById(id);
        return new ResponseEntity<>("删除成功", HttpStatus.OK);
    }

    /**
     * 根据id更新
     *
     * @param networkEs 要修改的
     * @return 结果集
     */
    @PostMapping("updateById")
    public ResponseEntity<Object> updateById(@RequestBody NetworkEs networkEs) {

        this.networkEsService.updateById(networkEs);
        return new ResponseEntity<>("id = " + networkEs.getId() + "更新成功", HttpStatus.NO_CONTENT);
    }

    /**
     * 分页查询
     *
     * @param pageable 分页条件
     * @return
     */
    @PostMapping("findList")
    public ResponseEntity<Object> findList(@RequestParam(required = false) String id, Pageable pageable) {
        return new ResponseEntity<>(this.networkEsService.findListRecent(id, pageable), HttpStatus.OK);
    }
}

