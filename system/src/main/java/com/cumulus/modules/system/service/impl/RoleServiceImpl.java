package com.cumulus.modules.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.cumulus.enums.DataScopeEnum;
import com.cumulus.exception.BadRequestException;
import com.cumulus.exception.EntityExistException;
import com.cumulus.modules.security.service.UserCacheClean;
import com.cumulus.modules.system.dto.RoleDto;
import com.cumulus.modules.system.dto.RoleQueryCriteria;
import com.cumulus.modules.system.dto.SimpRoleDto;
import com.cumulus.modules.system.dto.UserDto;
import com.cumulus.modules.system.entity.Menu;
import com.cumulus.modules.system.entity.Role;
import com.cumulus.modules.system.entity.User;
import com.cumulus.modules.system.mapstruct.RoleMapper;
import com.cumulus.modules.system.mapstruct.SimpRoleMapper;
import com.cumulus.modules.system.repository.RoleRepository;
import com.cumulus.modules.system.repository.UserRepository;
import com.cumulus.modules.system.service.RoleService;
import com.cumulus.utils.CacheKey;
import com.cumulus.utils.FileUtils;
import com.cumulus.utils.QueryUtils;
import com.cumulus.utils.RedisUtils;
import com.cumulus.utils.StringUtils;
import com.cumulus.utils.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 系统角色服务实现
 *
 * @author shenjc
 */
@Service
@CacheConfig(cacheNames = "role")
public class RoleServiceImpl implements RoleService {

    /**
     * 系统角色数据访问接口
     */
    @Autowired
    private RoleRepository roleRepository;

    /**
     * 系统角色传输对象与系统角色实体的映射
     */
    @Autowired
    private RoleMapper roleMapper;

    /**
     * 精简的系统角色传输对象与系统角色实体的映射
     */
    @Autowired
    private SimpRoleMapper simpRoleMapper;

    /**
     * Redis工具类
     */
    @Autowired
    private RedisUtils redisUtils;

    /**
     * 用户数据访问接口
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * 用户缓存清理服务
     */
    @Autowired
    private UserCacheClean userCacheClean;

    @Override
    public List<RoleDto> queryAll() {
        Sort sort = Sort.by(Sort.Direction.ASC, "level");
        return roleMapper.toDto(roleRepository.findAll(sort));
    }

    @Override
    public List<RoleDto> queryAll(RoleQueryCriteria criteria) {
        return roleMapper.toDto(roleRepository.findAll((root, criteriaQuery, criteriaBuilder) ->
                QueryUtils.getPredicate(root, criteria, criteriaBuilder)));
    }

    @Override
    public Page<RoleDto> queryAll(RoleQueryCriteria criteria, Pageable pageable) {
        Page<Role> page = roleRepository.findAll((root, criteriaQuery, criteriaBuilder) ->
                QueryUtils.getPredicate(root, criteria, criteriaBuilder), pageable);
        return page.map(roleMapper::toDto);
    }

    @Override
    @Cacheable(key = "'id:' + #p0")
    @Transactional(rollbackFor = Exception.class)
    public RoleDto findById(long id) {
        Role role = roleRepository.findById(id).orElseGet(Role::new);
        ValidationUtils.isNull(role.getId(), "Role", "id", id);
        return roleMapper.toDto(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Role resources) {
        checkCreate(resources);
        resources.setDataScope(resources.getDataScope() == null ?
                DataScopeEnum.ALL.getValue() : resources.getDataScope());
        roleRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Role resources) {
        Role oldRole = roleRepository.findById(resources.getId()).orElseGet(Role::new);
        checkUpdate(oldRole, resources);
        roleRepository.save(oldRole);
        // 删除相关缓存
        delCaches(oldRole.getId(), null);
    }

    @Override
    public void updateMenuAndUser(Role resources, RoleDto roleDTO) {
        Role role = roleMapper.toEntity(roleDTO);
        List<User> users = userRepository.findByRoleId(role.getId());
        // 更新角色关联的菜单
        role.setMenus(resources.getMenus() == null ? role.getMenus() : resources.getMenus());
        role.setUsers(resources.getUsers() == null ? role.getUsers() : resources.getUsers());
        delCaches(resources.getId(), users);
        roleRepository.save(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void untiedMenu(Long menuId) {
        // 更新菜单
        roleRepository.untiedMenu(menuId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        for (Long id : ids) {
            // 更新相关缓存
            delCaches(id, null);
        }
        roleRepository.deleteAllByIdIn(ids);
    }

    @Override
    public List<SimpRoleDto> findByUsersId(Long id) {
        return simpRoleMapper.toDto(new ArrayList<>(roleRepository.findByUserId(id)));
    }

    @Override
    public Integer findByRoles(Set<Role> roles) {
        if (roles.size() == 0) {
            return Integer.MAX_VALUE;
        }
        Set<RoleDto> roleDtos = new HashSet<>();
        for (Role role : roles) {
            roleDtos.add(findById(role.getId()));
        }
        return Collections.min(roleDtos.stream().map(RoleDto::getLevel).collect(Collectors.toList()));
    }

    @Override
    @Cacheable(key = "'auth:' + #p0.id")
    @Transactional(rollbackFor = Exception.class)
    public List<GrantedAuthority> mapToGrantedAuthorities(UserDto user) {
        Set<String> permissions = new HashSet<>();
        // 如果是管理员直接返回
        if (user.getIsAdmin()) {
            permissions.add("admin");
            return permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        }
        Set<Role> roles = roleRepository.findByUserId(user.getId());
        permissions = roles.stream().flatMap(role -> role.getMenus().stream()).map(Menu::getPermission)
                .filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        return permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override
    public void download(List<RoleDto> roles, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (RoleDto role : roles) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("角色名称", role.getName());
            map.put("角色级别", role.getLevel());
            map.put("描述", role.getDescription());
            map.put("创建日期", role.getCreateTime());
            list.add(map);
        }
        FileUtils.downloadExcel(list, response);
    }

    @Override
    public void verification(Set<Long> ids) {
        if (userRepository.countByRoles(ids) > 0) {
            throw new BadRequestException("所选角色存在用户关联，请解除关联再试！");
        }
    }

    @Override
    public List<Role> findInMenuId(List<Long> menuIds) {
        return roleRepository.findInMenuId(menuIds);
    }

    /**
     * 清理缓存
     *
     * @param id    角色ID
     * @param users 用户列表
     */
    public void delCaches(Long id, List<User> users) {
        users = CollectionUtil.isEmpty(users) ? userRepository.findByRoleId(id) : users;
        if (CollectionUtil.isNotEmpty(users)) {
            users.forEach(item -> userCacheClean.cleanUserCache(item.getUsername()));
            Set<Long> userIds = users.stream().map(User::getId).collect(Collectors.toSet());
            redisUtils.delByKeys(CacheKey.DATA_USER, userIds);
            redisUtils.delByKeys(CacheKey.MENU_USER, userIds);
            redisUtils.delByKeys(CacheKey.ROLE_AUTH, userIds);
        }
        redisUtils.del(CacheKey.ROLE_ID + id);
    }

    /**
     * 校验新增的权限组
     *
     * @param resources 新增的 权限对象
     */
    private void checkCreate(Role resources) {
        if (StringUtils.isBlank(resources.getName())) {
            throw new BadRequestException("权限组名为空");
        }
        if (null != roleRepository.findByName(resources.getName())) {
            throw new BadRequestException("权限组名字已存在");
        }
    }

    /**
     * 校验新增的权限组
     *
     * @param oldRole 新增的 权限对象
     */
    private void checkUpdate(Role oldRole, Role newRole) {
        ValidationUtils.isNull(oldRole.getId(), "Role", "id", newRole.getId());
        if (StringUtils.isNotBlank(newRole.getName())) {
            Role roleByName = roleRepository.findByName(newRole.getName());
            if (null != roleByName && !roleByName.getId().equals(oldRole.getId())) {
                throw new BadRequestException("权限组名已存在");
            }
            oldRole.setName(newRole.getName());
        }
        oldRole.setDescription(newRole.getDescription() == null ?
                oldRole.getDescription() : newRole.getDescription());
    }
}
