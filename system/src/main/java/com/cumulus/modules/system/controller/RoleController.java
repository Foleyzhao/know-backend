package com.cumulus.modules.system.controller;

import cn.hutool.core.lang.Dict;
import com.cumulus.base.BaseEntity;
import com.cumulus.exception.BadRequestException;
import com.cumulus.modules.system.dto.RoleDto;
import com.cumulus.modules.system.dto.RoleQueryCriteria;
import com.cumulus.modules.system.dto.SimpRoleDto;
import com.cumulus.modules.system.entity.Role;
import com.cumulus.modules.system.service.RoleService;
import com.cumulus.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色控制层
 *
 * @author shenjc
 */
@RestController
@RequestMapping("/api/roles")
@PreAuthorize("@auth.check('organizationalManagement')")
public class RoleController {

    /**
     * 角色服务接口
     */
    @Autowired
    private RoleService roleService;

    /**
     * 查询全部权限组
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return 角色列表
     */
    @GetMapping
    public ResponseEntity<Object> query(RoleQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(roleService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    /**
     * 新增角色
     *
     * @param resources 角色
     * @return 响应
     */
    @PostMapping
    public ResponseEntity<Object> create(@Validated(BaseEntity.Create.class) @RequestBody Role resources) {
        if (null != resources.getId()) {
            throw new BadRequestException("A new role cannot already have an ID");
        }
        resources.setLevel(resources.getLevel() == null ? Role.DEFAULT_LEVEL : resources.getLevel());
        getLevels(resources.getLevel());
        roleService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 修改角色
     *
     * @param resources 角色
     * @return 角色
     */
    @PutMapping
    public ResponseEntity<Object> update(@Validated(Role.Update.class) @RequestBody Role resources) {
        getLevels(resources.getLevel());
        roleService.update(resources);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 删除角色
     *
     * @param ids 角色ID集合
     * @return 响应
     */
    @DeleteMapping
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        for (Long id : ids) {
            RoleDto role = roleService.findById(id);
            getLevels(role.getLevel());
        }
        // 验证是否被用户关联
        roleService.verification(ids);
        roleService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 修改角色菜单
     *
     * @param resources 角色
     * @return 响应
     */
    @PutMapping(value = "/updateMenuAndUser")
    public ResponseEntity<Object> updateMenuAndUser(@RequestBody Role resources) {
        RoleDto role = roleService.findById(resources.getId());
        getLevels(role.getLevel());
        roleService.updateMenuAndUser(resources, role);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 根据角色ID查询角色
     *
     * @param id 角色ID
     * @return 角色
     */
    @GetMapping(value = "/{id}")
    public ResponseEntity<Object> query(@PathVariable Long id) {
        return new ResponseEntity<>(roleService.findById(id), HttpStatus.OK);
    }

    /**
     * 导出角色数据
     *
     * @param response 响应
     * @param criteria 查询参数
     * @throws IOException 异常
     */
    @GetMapping(value = "/download")
    public void download(HttpServletResponse response, RoleQueryCriteria criteria) throws IOException {
        roleService.download(roleService.queryAll(criteria), response);
    }

    /**
     * 获取全部的角色
     *
     * @return 角色列表
     */
    @GetMapping(value = "/all")
    public ResponseEntity<Object> query() {
        return new ResponseEntity<>(roleService.queryAll(), HttpStatus.OK);
    }

    /**
     * 获取用户的角色级别
     *
     * @return 用户的角色级别
     */
    @GetMapping(value = "/level")
    public ResponseEntity<Object> getLevel() {
        return new ResponseEntity<>(Dict.create().set("level", getLevels(null)), HttpStatus.OK);
    }

    /**
     * 获取用户的角色级别
     *
     * @return 用户的角色级别
     */
    private int getLevels(Integer level) {
        //获取当前用户的 权限组中的 level字段列表
        List<Integer> levels = roleService.findByUsersId(SecurityUtils.getCurrentUserId()).stream()
                .map(SimpRoleDto::getLevel).collect(Collectors.toList());
        if (levels.isEmpty()) {
            throw new BadRequestException("权限不足");
        }
        //找出当前用户拥有的最小（最高）level
        int min = Collections.min(levels);
        if (null != level) {
            //操作的level不为null 且小(高)与当前用户的level 则无法操作
            if (level < min) {
                throw new BadRequestException("权限不足，你的角色级别：" + min + "，低于操作的角色级别：" + level);
            }
        }
        return min;
    }
}
