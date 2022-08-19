package com.cumulus.modules.business.other.controller;

import com.cumulus.modules.business.other.entity.es.FlawEs;
import com.cumulus.modules.business.other.service.FlawEsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 风险控制层
 *
 * @author Shijh
 */
@RestController
@RequestMapping("api/es/flaw/")
public class FlawEsController {

    /**
     * 风险接口服务
     */
    @Autowired
    private FlawEsService flawEsService;

    /**
     * 批量添加
     *
     * @param flawEs 参数列表
     * @return 结果集
     */
    @PostMapping("saveBatch")
    public ResponseEntity<Object> saveBatch(@RequestBody List<FlawEs> flawEs) {
        if (CollectionUtils.isEmpty(flawEs)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.flawEsService.saveAll(flawEs);
        return new ResponseEntity<>("新增成功", HttpStatus.CREATED);
    }

    /**
     * 分页查询
     *
     * @param pageable 分页条件
     * @return 结果集
     */
    @PostMapping("findAll")
    public ResponseEntity<Object> finAll(Pageable pageable) {
        return new ResponseEntity<>(this.flawEsService.finAll(pageable), HttpStatus.OK);
    }

}
