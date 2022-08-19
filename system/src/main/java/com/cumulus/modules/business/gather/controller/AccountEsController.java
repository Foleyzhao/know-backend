package com.cumulus.modules.business.gather.controller;

import java.util.List;

import com.cumulus.modules.business.gather.entity.es.AccountEs;
import com.cumulus.modules.business.gather.service.AccountEsService;

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
 * 账号信息控制层
 *
 * @author Shijh
 */
@RestController
@RequestMapping("/api/es/account/")
@PreAuthorize("@auth.check('assetWarehouse')")
public class AccountEsController {

    /**
     * 账号信息服务接口
     */
    @Autowired
    private AccountEsService accountEsService;

    /**
     * 批量添加
     *
     * @param accountEsList 参数列表
     * @return 结果集
     */
    @PostMapping("saveBatch")
    public ResponseEntity<Object> saveBatch(@RequestBody List<AccountEs> accountEsList) {
        if (CollectionUtils.isEmpty(accountEsList)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.accountEsService.saveAll(accountEsList);
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
        this.accountEsService.deleteById(id);
        return new ResponseEntity<>("删除成功", HttpStatus.OK);
    }

    /**
     * 根据id更新
     *
     * @param accountEs 要修改的
     * @return 结果集
     */
    @PostMapping("updateById")
    public ResponseEntity<Object> updateById(@RequestBody AccountEs accountEs) {

        this.accountEsService.updateById(accountEs);
        return new ResponseEntity<>("更新成功", HttpStatus.OK);
    }

    /**
     * 分页查询
     *
     * @param id       资产id
     * @param pageable 分页条件
     * @return 返回分页信息
     */
    @PostMapping("findList")
    public ResponseEntity<Object> findList(@RequestParam(required = false) String id, Pageable pageable) {
        return new ResponseEntity<>(this.accountEsService.findListRecent(id, pageable), HttpStatus.OK);
    }

}
