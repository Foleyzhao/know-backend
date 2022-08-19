package com.cumulus.modules.business.detect.controller;

import javax.annotation.Resource;
import com.cumulus.annotation.rest.AnonymousPostMapping;
import com.cumulus.exception.BadRequestException;
import com.cumulus.modules.business.detect.dto.DetectTaskDto;
import com.cumulus.modules.business.detect.dto.DetectTaskQueryCriteria;
import com.cumulus.modules.business.detect.service.DetectRecordService;
import com.cumulus.modules.business.detect.service.DetectTaskService;
import com.cumulus.modules.business.dto.BatchPackage;
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
 * 发现任务控制层
 *
 * @author zhangxq
 */
@RestController
@RequestMapping("/api/detect")
@PreAuthorize("@auth.check('discoverAssets')")
public class DetectTaskController {

    /**
     * 发现任务服务接口
     */
    @Resource
    private DetectTaskService detectTaskService;

    /**
     * 发现任务记录服务接口
     */
    @Resource
    private DetectRecordService detectRecordService;

    /**
     * 分页查询发现任务
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return 发现任务列表
     */
    @GetMapping("/query")
    public ResponseEntity<Object> queryAll(DetectTaskQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(detectTaskService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    /**
     * 检查名称
     *
     * @param name 新建任务名称
     * @return true 不重复 false 重复
     */
    @GetMapping("/checkName")
    public ResponseEntity<Object> check(@RequestParam String name) {
        return new ResponseEntity<>(detectTaskService.checkName(name), HttpStatus.OK);
    }


    /**
     * 分页查询发现任务记录
     *
     * @param pageable 分页参数
     * @return 发现任务列表
     */
    @GetMapping("/record")
    public ResponseEntity<Object> queryRecord(Long id, Pageable pageable) {
        return new ResponseEntity<>(detectRecordService.queryByTaskId(id, pageable), HttpStatus.OK);
    }

    /**
     * 查询发现任务记录详情
     *
     * @param id 记录id
     * @return 记录详情列表
     */
    @GetMapping("/recordDetail")
    public ResponseEntity<Object> queryDetail(Long id) {
        return new ResponseEntity<>(detectRecordService.queryDetails(id), HttpStatus.OK);
    }

    /**
     * 新增发现任务
     *
     * @param detectTaskDto 发现任务
     * @return 结果
     */
//    @PostMapping("/create")
    @AnonymousPostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody DetectTaskDto detectTaskDto) {
        detectTaskService.create(detectTaskDto);
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    /**
     * 判断ip数量
     *
     * @param detectTaskDto 发现任务
     * @return 结果
     */
//    @PostMapping("/create")
    @AnonymousPostMapping("/isCreate")
    public ResponseEntity<Object> isCreate(@RequestBody DetectTaskDto detectTaskDto) {
        String create = detectTaskService.isCreate(detectTaskDto);
        if (create.equals(BadRequestException.HINT)) {
            throw new BadRequestException(BadRequestException.HINT);
        }
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    /**
     * 修改发现任务
     *
     * @param detectTaskDto 发现任务传输对象
     * @return 结果
     */
    @PutMapping("/update")
    public ResponseEntity<Object> update(@RequestBody DetectTaskDto detectTaskDto) {
        detectTaskService.updateById(detectTaskDto);
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    /**
     * 删除发现任务
     *
     * @param id 发现任务主键
     * @return 发现任务传输对象
     */
    @DeleteMapping("/del")
    public ResponseEntity<Object> delete(Long id) {
        detectTaskService.removeById(id);
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    /**
     * 批量删除发现任务
     *
     * @param batchPackage 发现任务列表
     * @return 发现任务列表
     */
    @DeleteMapping("/delBatch")
    public ResponseEntity<Object> deleteBatch(@RequestBody BatchPackage batchPackage) {
        detectTaskService.removeBatch(batchPackage.getIds(), batchPackage.isAll());
        return new ResponseEntity<>("", HttpStatus.OK);
    }


    /**
     * 单个执行
     *
     * @param id 任务id
     */
    @GetMapping("/execute")
    public ResponseEntity<Object> execute(@RequestParam Long id) {
        detectTaskService.execute(id);
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    /**
     * 批量执行
     *
     * @param batchPackage
     * @return
     */
    @PostMapping("/executeBatch")
    public ResponseEntity<Object> executeBatch(@RequestBody BatchPackage batchPackage) {
        detectTaskService.execute(batchPackage.getIds(), batchPackage.isAll());
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    /**
     * 取消
     *
     * @param id 任务id
     */
    @GetMapping("/cancel")
    public ResponseEntity<Object> cancel(@RequestParam Long id) {
        detectTaskService.cancel(id);
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    /**
     * 暂停/继续
     *
     * @param id 任务id
     */
    @GetMapping("/pause")
    public ResponseEntity<Object> pause(@RequestParam Long id) {
        detectTaskService.pause(id);
        return new ResponseEntity<>("", HttpStatus.OK);
    }
}
