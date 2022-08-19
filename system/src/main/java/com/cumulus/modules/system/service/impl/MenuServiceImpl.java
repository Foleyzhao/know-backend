package com.cumulus.modules.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.cumulus.exception.BadRequestException;
import com.cumulus.exception.EntityExistException;
import com.cumulus.modules.system.dto.MenuDto;
import com.cumulus.modules.system.dto.MenuQueryCriteria;
import com.cumulus.modules.system.dto.MenuTreeDto;
import com.cumulus.modules.system.dto.SimpRoleDto;
import com.cumulus.modules.system.entity.Menu;
import com.cumulus.modules.system.entity.Role;
import com.cumulus.modules.system.entity.User;
import com.cumulus.modules.system.mapstruct.MenuMapper;
import com.cumulus.modules.system.mapstruct.MenuTreeMapper;
import com.cumulus.modules.system.repository.MenuRepository;
import com.cumulus.modules.system.repository.UserRepository;
import com.cumulus.modules.system.service.MenuService;
import com.cumulus.modules.system.service.RoleService;
import com.cumulus.modules.system.vo.MenuMetaVo;
import com.cumulus.modules.system.vo.MenuVo;
import com.cumulus.utils.CacheKey;
import com.cumulus.utils.FileUtils;
import com.cumulus.utils.QueryUtils;
import com.cumulus.utils.RedisUtils;
import com.cumulus.utils.StringUtils;
import com.cumulus.utils.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 *
 * @author shenjc
 */
@Service
@CacheConfig(cacheNames = "menu")
public class MenuServiceImpl implements MenuService {

    /**
     * 菜单数据访问接口
     */
    @Autowired
    private MenuRepository menuRepository;

    /**
     * 用户数据访问接口
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * 系统菜单传输对象与系统菜单实体的映射
     */
    @Autowired
    private MenuMapper menuMapper;

    /**
     * 系统菜单树结构Mapper
     */
    @Autowired
    private MenuTreeMapper menuTreeMapper;

    /**
     * 角色服务接口
     */
    @Autowired
    private RoleService roleService;

    /**
     * redis工具类
     */
    @Autowired
    private RedisUtils redisUtils;

    @Override
    public List<MenuDto> queryAll(MenuQueryCriteria criteria, Boolean isQuery) throws Exception {
        Sort sort = Sort.by(Sort.Direction.ASC, "menuSort");
        if (Boolean.TRUE.equals(isQuery)) {
            criteria.setPidIsNull(true);
            List<Field> fields = QueryUtils.getAllFields(criteria.getClass(), new ArrayList<>());
            for (Field field : fields) {
                //设置对象的访问权限，保证对private的属性的访问
                field.setAccessible(true);
                Object val = field.get(criteria);
                if ("pidIsNull".equals(field.getName())) {
                    continue;
                }
                if (ObjectUtil.isNotNull(val)) {
                    criteria.setPidIsNull(null);
                    break;
                }
            }
        }
        return menuMapper.toDto(menuRepository.findAll((root, criteriaQuery, criteriaBuilder) ->
                QueryUtils.getPredicate(root, criteria, criteriaBuilder), sort));
    }

    @Override
    public List<MenuTreeDto> queryAllTree() {
        List<Menu> all = menuRepository.findAllByHidden(false);
        Map<Long, MenuTreeDto> treeMap = new LinkedHashMap<>();
        for (Menu menu : all) {
            treeMap.put(menu.getId(), menuTreeMapper.toDto(menu));
        }
        for (Map.Entry<Long, MenuTreeDto> treeEntry : treeMap.entrySet()) {
            final MenuTreeDto treeDto = treeEntry.getValue();
            if (treeDto.getPid() != null) {
                treeMap.get(treeDto.getPid()).getChildren().add(treeDto);
            }
        }
        List<MenuTreeDto> treeDtoList = new ArrayList<>();
        treeMap.forEach((id, menuTreeDto) -> {
            if (menuTreeDto.getPid() == null) {
                treeDtoList.add(menuTreeDto);
            }
        });
        return treeDtoList;
    }

    @Override
    public List<MenuDto> queryAllByUser(Long userId) {
        if (userId != null) {
            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent()) {
                return new ArrayList<>();
            }
            Set<Long> collect = userOpt.get().getRoles().stream().map(Role::getId).collect(Collectors.toSet());
            return menuMapper.toDto(new ArrayList<>(menuRepository.findByRoleIds(collect)));
        }
        return menuMapper.toDto(menuRepository.findAll());
    }

    @Override
    @Cacheable(key = "'id:' + #p0")
    public MenuDto findById(long id) {
        Menu menu = menuRepository.findById(id).orElseGet(Menu::new);
        ValidationUtils.isNull(menu.getId(), "Menu", "id", id);
        return menuMapper.toDto(menu);
    }

    @Override
    @Cacheable(key = "'user:' + #p0")
    public List<MenuDto> findByUser(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        //用户为管理员时返回全部权限
        if (userOpt.isPresent() && userOpt.get().getIsAdmin()) {
            return menuMapper.toDto(menuRepository.findAll());
        }
        List<SimpRoleDto> roles = roleService.findByUsersId(userId);
        Set<Long> roleIds = roles.stream().map(SimpRoleDto::getId).collect(Collectors.toSet());
        Set<Menu> menus = menuRepository.findByRoleIds(roleIds);
        return menus.stream().map(menuMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<MenuDto> queryAllByRole(Long roleId) {
        return menuMapper.toDto(new ArrayList<>(menuRepository.
                findByRoleIds(new HashSet<>(Collections.singletonList(roleId)))));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Menu resources) {
        if (null != menuRepository.findByTitle(resources.getTitle())) {
            throw new BadRequestException("标题已存在");
        }
        if (StringUtils.isNotBlank(resources.getComponentName())) {
            if (null != menuRepository.findByComponentName(resources.getComponentName())) {
                throw new BadRequestException("组件名已存在");
            }
        }
        if (resources.getPid().equals(0L)) {
            resources.setPid(null);
        }
        if (resources.getIFrame()) {
            String http = "http://", https = "https://";
            if (!(resources.getPath().toLowerCase().startsWith(http) ||
                    resources.getPath().toLowerCase().startsWith(https))) {
                throw new BadRequestException("外链必须以http://或者https://开头");
            }
        }
        menuRepository.save(resources);
        // 计算子节点数目
        resources.setSubCount(0);
        // 更新父节点菜单数目
        updateSubCnt(resources.getPid());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Menu resources) {
        if (resources.getId().equals(resources.getPid())) {
            throw new BadRequestException("上级不能为自己");
        }
        Menu menu = menuRepository.findById(resources.getId()).orElseGet(Menu::new);
        ValidationUtils.isNull(menu.getId(), "Permission", "id", resources.getId());

        if (resources.getIFrame()) {
            String http = "http://", https = "https://";
            if (!(resources.getPath().toLowerCase().startsWith(http) ||
                    resources.getPath().toLowerCase().startsWith(https))) {
                throw new BadRequestException("外链必须以http://或者https://开头");
            }
        }
        Menu menu1 = menuRepository.findByTitle(resources.getTitle());

        if (null != menu1 && !menu1.getId().equals(menu.getId())) {
            throw new BadRequestException("标题已存在");
        }

        if (resources.getPid().equals(0L)) {
            resources.setPid(null);
        }

        // 记录的父节点ID
        Long oldPid = menu.getPid();
        Long newPid = resources.getPid();

        if (StringUtils.isNotBlank(resources.getComponentName())) {
            menu1 = menuRepository.findByComponentName(resources.getComponentName());
            if (null != menu1 && !menu1.getId().equals(menu.getId())) {
                throw new BadRequestException("组件名已存在");
            }
        }
        menu.setTitle(resources.getTitle());
        menu.setComponent(resources.getComponent());
        menu.setPath(resources.getPath());
        menu.setIcon(resources.getIcon());
        menu.setIFrame(resources.getIFrame());
        menu.setPid(resources.getPid());
        menu.setMenuSort(resources.getMenuSort());
        menu.setCache(resources.getCache());
        menu.setHidden(resources.getHidden());
        menu.setComponentName(resources.getComponentName());
        menu.setPermission(resources.getPermission());
        menu.setType(resources.getType());
        menuRepository.save(menu);
        // 计算父级菜单节点数目
        updateSubCnt(oldPid);
        updateSubCnt(newPid);
        // 清理缓存
        delCaches(resources.getId());
    }

    @Override
    public Set<Menu> getChildMenus(List<Menu> menuList, Set<Menu> menuSet) {
        for (Menu menu : menuList) {
            menuSet.add(menu);
            List<Menu> menus = menuRepository.findByPid(menu.getId());
            if (null != menus && menus.size() != 0) {
                getChildMenus(menus, menuSet);
            }
        }
        return menuSet;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Menu> menuSet) {
        for (Menu menu : menuSet) {
            // 清理缓存
            delCaches(menu.getId());
            roleService.untiedMenu(menu.getId());
            menuRepository.deleteById(menu.getId());
            updateSubCnt(menu.getPid());
        }
    }

    @Override
    public List<MenuDto> getMenus(Long pid) {
        List<Menu> menus;
        if (null != pid && !pid.equals(0L)) {
            menus = menuRepository.findByPid(pid);
        } else {
            menus = menuRepository.findByPidIsNull();
        }
        return menuMapper.toDto(menus);
    }

    @Override
    public List<MenuDto> getSuperior(MenuDto menuDto, List<Menu> menus) {
        if (null == menuDto.getPid()) {
            menus.addAll(menuRepository.findByPidIsNull());
            return menuMapper.toDto(menus);
        }
        menus.addAll(menuRepository.findByPid(menuDto.getPid()));
        return getSuperior(findById(menuDto.getPid()), menus);
    }

    @Override
    public List<MenuDto> buildTree(List<MenuDto> menuDtos) {
        List<MenuDto> trees = new ArrayList<>();
        Set<Long> ids = new HashSet<>();
        for (MenuDto menuDTO : menuDtos) {
            if (null == menuDTO.getPid()) {
                trees.add(menuDTO);
            }
            for (MenuDto it : menuDtos) {
                if (menuDTO.getId().equals(it.getPid())) {
                    if (null == menuDTO.getChildren()) {
                        menuDTO.setChildren(new ArrayList<>());
                    }
                    menuDTO.getChildren().add(it);
                    ids.add(it.getId());
                }
            }
        }
        if (trees.size() == 0) {
            trees = menuDtos.stream().filter(s -> !ids.contains(s.getId())).collect(Collectors.toList());
        }
        return trees;
    }

    @Override
    public List<MenuVo> buildMenus(List<MenuDto> menuDtos) {
        List<MenuVo> list = new LinkedList<>();
        menuDtos.forEach(menuDTO -> {
                    if (null != menuDTO) {
                        List<MenuDto> menuDtoList = menuDTO.getChildren();
                        MenuVo menuVo = new MenuVo();
                        menuVo.setName(ObjectUtil.isNotEmpty(menuDTO.getComponentName()) ?
                                menuDTO.getComponentName() : menuDTO.getTitle());
                        // 一级目录需要加斜杠，不然会报警告
                        menuVo.setPath(null == menuDTO.getPid() ? "/" + menuDTO.getPath() : menuDTO.getPath());
                        menuVo.setHidden(menuDTO.getHidden());
                        // 如果不是外链
                        if (!menuDTO.getIFrame()) {
                            if (null == menuDTO.getPid()) {
                                menuVo.setComponent(StringUtils.isEmpty(menuDTO.getComponent()) ?
                                        "Layout" : menuDTO.getComponent());
                                // 如果不是一级菜单，并且菜单类型为目录，则代表是多级菜单
                            } else if (menuDTO.getType() == 0) {
                                menuVo.setComponent(StringUtils.isEmpty(menuDTO.getComponent()) ?
                                        "ParentView" : menuDTO.getComponent());
                            } else if (StringUtils.isNoneBlank(menuDTO.getComponent())) {
                                menuVo.setComponent(menuDTO.getComponent());
                            }
                        }
                        menuVo.setMeta(new MenuMetaVo(menuDTO.getTitle(), menuDTO.getIcon(), !menuDTO.getCache()));
                        if (CollectionUtil.isNotEmpty(menuDtoList)) {
                            menuVo.setAlwaysShow(true);
                            menuVo.setRedirect("noredirect");
                            menuVo.setChildren(buildMenus(menuDtoList));
                            // 处理是一级菜单并且没有子菜单的情况
                        } else if (null == menuDTO.getPid()) {
                            MenuVo menuVo1 = new MenuVo();
                            menuVo1.setMeta(menuVo.getMeta());
                            // 非外链
                            if (!menuDTO.getIFrame()) {
                                menuVo1.setPath("index");
                                menuVo1.setName(menuVo.getName());
                                menuVo1.setComponent(menuVo.getComponent());
                            } else {
                                menuVo1.setPath(menuDTO.getPath());
                            }
                            menuVo.setName(null);
                            menuVo.setMeta(null);
                            menuVo.setComponent("Layout");
                            List<MenuVo> list1 = new ArrayList<>();
                            list1.add(menuVo1);
                            menuVo.setChildren(list1);
                        }
                        list.add(menuVo);
                    }
                }
        );
        return list;
    }

    @Override
    public Menu findOne(Long id) {
        Menu menu = menuRepository.findById(id).orElseGet(Menu::new);
        ValidationUtils.isNull(menu.getId(), "Menu", "id", id);
        return menu;
    }

    @Override
    public void download(List<MenuDto> menuDtos, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (MenuDto menuDTO : menuDtos) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("菜单标题", menuDTO.getTitle());
            map.put("菜单类型", null == menuDTO.getType() ? "目录" : menuDTO.getType() == 1 ? "菜单" : "按钮");
            map.put("权限标识", menuDTO.getPermission());
            map.put("外链菜单", menuDTO.getIFrame() ? "是" : "否");
            map.put("菜单可见", menuDTO.getHidden() ? "否" : "是");
            map.put("是否缓存", menuDTO.getCache() ? "是" : "否");
            map.put("创建日期", menuDTO.getCreateTime());
            list.add(map);
        }
        FileUtils.downloadExcel(list, response);
    }

    /**
     * 更新子菜单数量
     *
     * @param menuId 菜单ID
     */
    private void updateSubCnt(Long menuId) {
        if (null != menuId) {
            int count = menuRepository.countByPid(menuId);
            menuRepository.updateSubCntById(count, menuId);
        }
    }

    /**
     * 清理缓存
     *
     * @param id 菜单ID
     */
    public void delCaches(Long id) {
        List<User> users = userRepository.findByMenuId(id);
        redisUtils.del(CacheKey.MENU_ID + id);
        redisUtils.delByKeys(CacheKey.MENU_USER, users.stream().map(User::getId).collect(Collectors.toSet()));
        // 清除 Role 缓存
        List<Role> roles = roleService.findInMenuId(new ArrayList<Long>() {

            private static final long serialVersionUID = -3464864152656885340L;

            {
                add(id);
            }
        });
        redisUtils.delByKeys(CacheKey.ROLE_ID, roles.stream().map(Role::getId).collect(Collectors.toSet()));
    }
}
