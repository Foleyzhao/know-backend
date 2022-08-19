package com.cumulus.modules.business.gather.controller;

import java.util.List;

import com.cumulus.modules.business.gather.entity.es.HistoryEs;
import com.cumulus.modules.business.gather.service.HistoryEsService;

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
 * 历史变更控制层
 *
 * @author Shijh
 */
@RestController
@RequestMapping("/api/es/history/")
@PreAuthorize("@auth.check('assetWarehouse')")
public class HistoryEsController {

    /**
     * 历史变更服务接口
     */
    @Autowired
    private HistoryEsService historyEsService;

    /**
     * 批量添加
     *
     * @param historyEsList 参数列表
     * @return 结果集
     */
    @PostMapping("saveBatch")
    public ResponseEntity<Object> saveBatch(@RequestBody List<HistoryEs> historyEsList) {
        if (CollectionUtils.isEmpty(historyEsList)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.historyEsService.saveAll(historyEsList);
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
        this.historyEsService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 根据id更新
     *
     * @param historyEs 要修改的
     * @return 结果集
     */
    @PostMapping("updateById")
    public ResponseEntity<Object> updateById(@RequestBody HistoryEs historyEs) {

        this.historyEsService.updateById(historyEs);
        return new ResponseEntity<>("id = " + historyEs.getId() + "更新成功", HttpStatus.NO_CONTENT);
    }

    /**
     * 分页查询
     *
     * @param id       资产采集id
     * @param pageable 分页条件
     * @return 结果集
     */
    @PostMapping("findList")
    public ResponseEntity<Object> findList(@RequestParam(required = false) Long id, Pageable pageable) {
        return new ResponseEntity<>(this.historyEsService.findListRecent(id, pageable), HttpStatus.OK);
    }
}
