package com.cumulus.modules.system.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.cumulus.exception.BadRequestException;
import com.cumulus.modules.system.dto.MenuDto;
import com.cumulus.modules.system.dto.MenuQueryCriteria;
import com.cumulus.modules.system.dto.MenuTreeDto;
import com.cumulus.modules.system.entity.Menu;
import com.cumulus.modules.system.mapstruct.MenuMapper;
import com.cumulus.modules.system.service.MenuService;
import com.cumulus.utils.PageUtils;
import com.cumulus.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 菜单控制层
 *
 * @author shenjc
 */
@RestController
@RequestMapping("/api/menus")
@PreAuthorize("@auth.check('organizationalManagement')")
public class MenuController {

    /**
     * 菜单服务接口
     */
    @Autowired
    private MenuService menuService;

    /**
     * 系统菜单传输对象与系统菜单实体的映射
     */
    @Autowired
    private MenuMapper menuMapper;

    /**
     * 根据用户擦好像菜单
     *
     * @param userId 用户id
     * @return 菜单列表
     */
    @GetMapping("queryAllByUser")
    public ResponseEntity<Object> queryAllByUser(Long userId) {
        List<MenuDto> menuDtoList = menuService.queryAllByUser(userId);
        return new ResponseEntity<>(menuDtoList, HttpStatus.OK);
    }

    /**
     * 根据用户擦好像菜单
     *
     * @param roleId 用户id
     * @return 菜单列表
     */
    @GetMapping("queryAllByRole")
    public ResponseEntity<Object> queryAllByRole(Long roleId) {
        List<MenuDto> menuDtoList = menuService.queryAllByRole(roleId);
        return new ResponseEntity<>(menuDtoList, HttpStatus.OK);
    }

    /**
     * 根据用户擦好像菜单 树形结构
     *
     * @return 菜单列表 树形结构
     */
    @GetMapping("queryAll")
    @PreAuthorize("@auth.check(@auth.NO_PERMISSION)")
    public ResponseEntity<Object> queryAllTree() {
        List<MenuTreeDto> menuDtoList = menuService.queryAllTree();
        return new ResponseEntity<>(menuDtoList, HttpStatus.OK);
    }

    /**
     * 导出菜单数据
     *
     * @param response 响应
     * @param criteria 查询参数
     * @throws Exception 异常
     */
    @GetMapping(value = "/download")
    public void download(HttpServletResponse response, MenuQueryCriteria criteria) throws Exception {
        menuService.download(menuService.queryAll(criteria, false), response);
    }

    /**
     * 获取当前用户拥有的菜单
     *
     * @return 菜单列表
     */
    @GetMapping(value = "/build")
    public ResponseEntity<Object> buildMenus() {
        List<MenuDto> menuDtoList = menuService.findByUser(SecurityUtils.getCurrentUserId());
        return new ResponseEntity<>(menuService.buildTree(menuDtoList), HttpStatus.OK);
    }

    /**
     * 根据父菜单ID获取子菜单
     *
     * @param pid 父菜单ID
     * @return 菜单列表
     */
    @GetMapping(value = "/lazy")
    public ResponseEntity<Object> query(@RequestParam Long pid) {
        return new ResponseEntity<>(menuService.getMenus(pid), HttpStatus.OK);
    }

    /**
     * 根据菜单ID递归获取所有后代菜单ID（包含自身ID）
     *
     * @param id 菜单ID
     * @return 菜单ID集合
     */
    @GetMapping(value = "/child")
    public ResponseEntity<Object> child(@RequestParam Long id) {
        Set<Menu> menuSet = new HashSet<>();
        List<MenuDto> menuList = menuService.getMenus(id);
        menuSet.add(menuService.findOne(id));
        menuSet = menuService.getChildMenus(menuMapper.toEntity(menuList), menuSet);
        Set<Long> ids = menuSet.stream().map(Menu::getId).collect(Collectors.toSet());
        return new ResponseEntity<>(ids, HttpStatus.OK);
    }

    /**
     * 查询菜单
     *
     * @param criteria 查询参数
     * @return 菜单列表
     * @throws Exception 异常
     */
    @GetMapping
    public ResponseEntity<Object> query(MenuQueryCriteria criteria) throws Exception {
        List<MenuDto> menuDtoList = menuService.queryAll(criteria, true);
        return new ResponseEntity<>(PageUtils.toPage(menuDtoList, menuDtoList.size()), HttpStatus.OK);
    }

    /**
     * 根据菜单ID集合递归获取同级与上级菜单
     *
     * @param ids 菜单ID集合
     * @return 菜单列表
     */
    @PostMapping("/superior")
    public ResponseEntity<Object> getSuperior(@RequestBody List<Long> ids) {
        Set<MenuDto> menuDtos = new LinkedHashSet<>();
        if (CollectionUtil.isNotEmpty(ids)) {
            for (Long id : ids) {
                MenuDto menuDto = menuService.findById(id);
                menuDtos.addAll(menuService.getSuperior(menuDto, new ArrayList<>()));
            }
            return new ResponseEntity<>(menuService.buildTree(new ArrayList<>(menuDtos)), HttpStatus.OK);
        }
        return new ResponseEntity<>(menuService.getMenus(null), HttpStatus.OK);
    }

    /**
     * 新增菜单
     *
     * @param resources 菜单
     * @return 响应
     */
    @PostMapping
    public ResponseEntity<Object> create(@Validated @RequestBody Menu resources) {
        if (null != resources.getId()) {
            throw new BadRequestException("A new menu cannot already have an ID");
        }
        menuService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 修改菜单
     *
     * @param resources 菜单
     * @return 响应
     */
    @PutMapping
    public ResponseEntity<Object> update(@Validated(Menu.Update.class) @RequestBody Menu resources) {
        menuService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 删除菜单
     *
     * @param ids 菜单ID集合
     * @return 响应
     */
    @DeleteMapping
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        Set<Menu> menuSet = new HashSet<>();
        for (Long id : ids) {
            List<MenuDto> menuList = menuService.getMenus(id);
            menuSet.add(menuService.findOne(id));
            menuSet = menuService.getChildMenus(menuMapper.toEntity(menuList), menuSet);
        }
        menuService.delete(menuSet);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
