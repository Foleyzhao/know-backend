package com.cumulus.modules.system.controller;

import com.cumulus.annotation.Log;
import com.cumulus.exception.BadRequestException;
import com.cumulus.modules.system.dto.JobQueryCriteria;
import com.cumulus.modules.system.entity.Job;
import com.cumulus.modules.system.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * 岗位控制层
 */
@RestController
@RequestMapping("/api/job")
@PreAuthorize("@auth.check('organizationalManagement')")
public class JobController {

    /**
     * 岗位服务接口
     */
    @Autowired
    private JobService jobService;

    /**
     * 导出岗位数据
     *
     * @param response 响应
     * @param criteria 查询参数
     * @throws IOException 异常
     */
    @GetMapping(value = "/download")
    @PreAuthorize("@auth.check('job:list')")
    public void download(HttpServletResponse response, JobQueryCriteria criteria) throws IOException {
        jobService.download(jobService.queryAll(criteria), response);
    }

    /**
     * 查询岗位
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return 岗位列表
     */
    @GetMapping
    @PreAuthorize("@auth.check('job:list','user:list')")
    public ResponseEntity<Object> query(JobQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(jobService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    /**
     * 新增岗位
     *
     * @param resources 岗位
     * @return 响应
     */
    @Log("新增岗位")
    @PostMapping
    @PreAuthorize("@auth.check('job:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody Job resources) {
        if (null != resources.getId()) {
            throw new BadRequestException("A new job cannot already have an ID");
        }
        jobService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 修改岗位
     *
     * @param resources 岗位
     * @return 响应
     */
    @Log("修改岗位")
    @PutMapping
    @PreAuthorize("@auth.check('job:edit')")
    public ResponseEntity<Object> update(@Validated(Job.Update.class) @RequestBody Job resources) {
        jobService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 删除岗位
     *
     * @param ids 岗位ID集合
     * @return 响应
     */
    @Log("删除岗位")
    @DeleteMapping
    @PreAuthorize("@auth.check('job:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        // 验证是否被用户关联
        jobService.verification(ids);
        jobService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
