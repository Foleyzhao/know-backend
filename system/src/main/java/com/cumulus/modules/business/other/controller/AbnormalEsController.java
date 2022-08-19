package com.cumulus.modules.business.other.controller;

import com.cumulus.modules.business.other.entity.es.AbnormalEs;
import com.cumulus.modules.business.other.service.AbnormalEsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 异常控制层
 *
 * @author Shijh
 */
@RestController
@RequestMapping("api/es/abnormal/")
public class AbnormalEsController {

    /**
     * 异常服务接口
     */
    @Autowired
    private AbnormalEsService abnormalEsService;

    /**
     * 批量添加
     *
     * @param abnormalEsList 参数列表
     * @return 结果集
     */
    @PostMapping("saveBatch")
    public ResponseEntity<Object> saveBatch(@RequestBody List<AbnormalEs> abnormalEsList) {
        if (CollectionUtils.isEmpty(abnormalEsList)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.abnormalEsService.saveAll(abnormalEsList);
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
        this.abnormalEsService.deleteById(id);
        return new ResponseEntity<>("删除成功", HttpStatus.OK);
    }

    /**
     * 根据id更新
     *
     * @param abnormalEs 要修改的
     * @return 结果集
     */
    @PostMapping("updateById")
    public ResponseEntity<Object> updateById(@RequestBody AbnormalEs abnormalEs) {

        this.abnormalEsService.updateById(abnormalEs);
        return new ResponseEntity<>("更新成功", HttpStatus.OK);
    }

    /**
     * 分页查询
     *
     * @param id       资产id
     * @param pageable 分页条件
     * @return
     */
    @PostMapping("findList")
    public ResponseEntity<Object> findList(@RequestParam String id, Pageable pageable) {
        return new ResponseEntity<>(this.abnormalEsService.findList(id, pageable), HttpStatus.OK);
    }

    /**
     * 分页查询
     *
     * @param ip 资产ip
     * @return 结果集
     */
    @PostMapping("getAbnormal")
    public ResponseEntity<Object> getAbnormal(@RequestParam String ip, Pageable pageable) {
        return new ResponseEntity<>(this.abnormalEsService.getAbnormal(ip, pageable), HttpStatus.OK);
    }
}
