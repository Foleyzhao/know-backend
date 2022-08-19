package com.cumulus.modules.business.gather.controller;

import javax.annotation.Resource;

import com.cumulus.exception.BadRequestException;
import com.cumulus.modules.business.dto.BatchPackage;
import com.cumulus.modules.business.gather.dto.GatherTaskDto;
import com.cumulus.modules.business.gather.dto.GatherTaskQueryCriteria;
import com.cumulus.modules.business.gather.service.GatherPlanService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 采集任务控制层
 *
 * @author zhangxq
 */
@RestController
@RequestMapping("/api/gather")
@PreAuthorize("@auth.check('dataGather')")
public class GatherTaskController {

    /**
     * 采集任务服务接口
     */
    @Resource
    private GatherPlanService gatherPlanService;

    /**
     * 分页查询计划
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return 计划列表
     */
    @GetMapping("/query")
    public ResponseEntity<Object> queryAll(GatherTaskQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(gatherPlanService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    /**
     * 新增计划
     *
     * @param gatherTaskDto 计划
     * @return 结果
     */
    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody GatherTaskDto gatherTaskDto) {
        gatherPlanService.create(gatherTaskDto);
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    /**
     * 判断ip数量
     *
     * @param gatherTaskDto 计划
     * @return 结果
     */
    @PostMapping("/isCreate")
    public ResponseEntity<Object> isCreate(@RequestBody GatherTaskDto gatherTaskDto) {
        String create = gatherPlanService.isCreate(gatherTaskDto);
        if (create.equals(BadRequestException.HINT)) {
            throw new BadRequestException(BadRequestException.HINT);
        }
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    /**
     * 修改计划
     *
     * @param gatherTaskDto 计划传输对象
     * @return 结果
     */
    @PutMapping("/update")
    public ResponseEntity<Object> update(@RequestBody GatherTaskDto gatherTaskDto) {
        gatherPlanService.updateById(gatherTaskDto);
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    /**
     * 删除计划
     *
     * @param id 计划主键
     * @return
     */
    @DeleteMapping("/del")
    public ResponseEntity<Object> delete(Long id) {
        gatherPlanService.removeById(id);
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    /**
     * 批量删除计划
     *
     * @param batchPackage
     * @return
     */
    @DeleteMapping("/delBatch")
    public ResponseEntity<Object> deleteBatch(@RequestBody BatchPackage batchPackage) {
        gatherPlanService.removeBatch(batchPackage.getIds(), batchPackage.isAll());
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    /**
     * 开始任务
     *
     * @param id 采集任务id
     * @return 结果
     */
    @GetMapping("/start")
    public ResponseEntity<Object> start(@RequestParam Long id) {
        gatherPlanService.start(id);
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    /**
     * 单个执行-开始任务
     *
     * @param assetId 资产id
     * @return 结果
     */
    @GetMapping("/startByAssetId")
    public ResponseEntity<Object> startByAssetId(@RequestParam Long assetId) {
        gatherPlanService.startByAssetId(assetId);
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    /**
     * 批量开始任务
     *
     * @param batchPackage
     * @return
     */
    @PostMapping("/startBatch")
    public ResponseEntity<Object> startBatch(@RequestBody BatchPackage batchPackage) {
        gatherPlanService.startBatch(batchPackage.getIds(), batchPackage.isAll());
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    /**
     * 暂停任务
     *
     * @param id id
     * @return 结果
     */
    @GetMapping("/pause")
    public ResponseEntity<Object> pause(@RequestParam Long id) {
        gatherPlanService.pause(id);
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    /**
     * 取消任务
     *
     * @param id id
     * @return 结果
     */
    @GetMapping("/cancel")
    public ResponseEntity<Object> cancel(@RequestParam Long id) {
        gatherPlanService.cancel(id);
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    /**
     * 检查名称
     *
     * @param name 新建任务名称
     * @return true 不重复 false 重复
     */
    @GetMapping("/checkName")
    public ResponseEntity<Object> check(@RequestParam String name) {
        return new ResponseEntity<>(gatherPlanService.checkName(name), HttpStatus.OK);
    }

    /**
     * 根据任务id查询资产列表
     *
     * @param id       id
     * @param pageable 分页
     * @return 资产列表
     */
    @GetMapping("/getAssets")
    public ResponseEntity<Object> getAssets(Pageable pageable, Long id) {
        return new ResponseEntity<>(gatherPlanService.findAssetsById(id, pageable), HttpStatus.OK);
    }


}
