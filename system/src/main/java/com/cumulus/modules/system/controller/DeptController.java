package com.cumulus.modules.system.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import com.cumulus.exception.BadRequestException;
import com.cumulus.modules.system.dto.DeptDto;
import com.cumulus.modules.system.dto.DeptQueryCriteria;
import com.cumulus.modules.system.entity.Dept;
import com.cumulus.modules.system.entity.Job;
import com.cumulus.modules.system.entity.User;
import com.cumulus.modules.system.service.DeptService;
import com.cumulus.modules.system.service.UserService;
import cn.hutool.core.collection.CollectionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 部门控制层
 *
 * @author shenjc
 */
@RestController
@RequestMapping("/api/dept")
@PreAuthorize("@auth.check('organizationalManagement')")
public class DeptController {

    /**
     * 系统部门服务接口
     */
    @Autowired
    private DeptService deptService;

    /**
     * 用户服务
     */
    @Autowired
    private UserService userService;

    /**
     * 查询部门
     *
     * @param criteria 查询参数
     * @param pageable 分页信息
     * @return 部门列表
     */
    @GetMapping
    @PreAuthorize("@auth.check(@auth.NO_PERMISSION)")
    public ResponseEntity<Object> query(DeptQueryCriteria criteria, Pageable pageable) throws Exception {
        Page<DeptDto> deptDtoPage = deptService.queryAll(criteria, pageable);
        return new ResponseEntity<>(deptDtoPage, HttpStatus.OK);
    }

    /**
     * 新增部门
     *
     * @param resources 部门
     * @return 响应
     */
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Object> create(@Validated(User.CreateDept.class) @RequestBody User resources) {
        if (null == resources.getDept() || resources.getDept().getId() != null) {
            throw new BadRequestException("A new dept cannot already have an ID");
        }
        deptService.create(resources.getDept());
        //创建管理员用户
        Job job = new Job();
        job.setId(Job.DEFAULT_DEPT_HEAD_JOB_ID);
        resources.setJob(job);
        userService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 修改部门
     *
     * @param resources 部门
     * @return 响应
     */
    @PutMapping
    public ResponseEntity<Object> update(@Validated(Dept.Update.class) @RequestBody Dept resources) {
        deptService.update(resources);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 删除部门
     *
     * @param ids 部门ID集合
     * @return 响应
     */
    @DeleteMapping
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        Set<DeptDto> deptDtos = new HashSet<>();
        for (Long id : ids) {
            List<Dept> deptList = deptService.findByPid(id);
            deptDtos.add(deptService.findById(id));
            if (CollectionUtil.isNotEmpty(deptList)) {
                deptDtos = deptService.getDeleteDepts(deptList, deptDtos);
            }
        }
        // 验证是否被角色或用户关联
        deptService.verification(deptDtos);
        deptService.delete(deptDtos);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 导出部门数据
     *
     * @param response 响应
     * @param criteria 查询参数
     * @throws Exception 异常
     */
    @GetMapping(value = "/download")
    public void download(HttpServletResponse response, DeptQueryCriteria criteria) throws Exception {
        deptService.download(deptService.queryAll(criteria, false), response);
    }

    /**
     * 根据id查找
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/getById")
    public ResponseEntity<Object> getById(Long id) {
        return new ResponseEntity<>(deptService.findById(id), HttpStatus.OK);
    }

    /**
     * @param ids 部门ID列表
     * @return 同级部门与上级部门数据
     * @deprecated 目前部门没有上下级之分
     * 根据部门ID获取同级部门与上级部门数据
     */
    @PostMapping("/superior")
    public ResponseEntity<Object> getSuperior(@RequestBody List<Long> ids) {
        Set<DeptDto> deptDtos = new LinkedHashSet<>();
        for (Long id : ids) {
            DeptDto deptDto = deptService.findById(id);
            List<DeptDto> depts = deptService.getSuperior(deptDto, new ArrayList<>());
            deptDtos.addAll(depts);
        }
        return new ResponseEntity<>(deptService.buildTree(new ArrayList<>(deptDtos)), HttpStatus.OK);
    }
}
