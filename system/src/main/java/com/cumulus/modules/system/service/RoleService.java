package com.cumulus.modules.system.service;

import com.cumulus.modules.system.entity.Role;
import com.cumulus.modules.system.dto.RoleDto;
import com.cumulus.modules.system.dto.RoleQueryCriteria;
import com.cumulus.modules.system.dto.SimpRoleDto;
import com.cumulus.modules.system.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * 系统角色服务接口
 *
 * @author shenjc
 */
public interface RoleService {

    /**
     * 查询全部角色
     *
     * @return 角色列表
     */
    List<RoleDto> queryAll();

    /**
     * 根据ID查询角色
     *
     * @param id 角色ID
     * @return 角色
     */
    RoleDto findById(long id);

    /**
     * 创建角色
     *
     * @param resources 角色
     */
    void create(Role resources);

    /**
     * 编辑角色
     *
     * @param resources 角色
     */
    void update(Role resources);

    /**
     * 根据角色ID集合删除角色
     *
     * @param ids 角色ID集合
     */
    void delete(Set<Long> ids);

    /**
     * 根据用户ID查询角色列表
     *
     * @param id 用户ID
     * @return 角色列表
     */
    List<SimpRoleDto> findByUsersId(Long id);

    /**
     * 根据角色集合查询角色级别
     *
     * @param roles 角色集合
     * @return 角色级别
     */
    Integer findByRoles(Set<Role> roles);

    /**
     * 更新角色关联的菜单
     *
     * @param resources 角色
     * @param roleDTO   角色传输对象
     */
    void updateMenuAndUser(Role resources, RoleDto roleDTO);

    /**
     * 根据菜单ID解除该菜单的所有角色绑定
     *
     * @param id 菜单ID
     */
    void untiedMenu(Long id);

    /**
     * 根据条件分页查询角色
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return 角色列表
     */
    Page<RoleDto> queryAll(RoleQueryCriteria criteria, Pageable pageable);

    /**
     * 根据条件查询全部角色
     *
     * @param criteria 查询参数
     * @return 角色列表
     */
    List<RoleDto> queryAll(RoleQueryCriteria criteria);

    /**
     * 导出角色列表
     *
     * @param queryAll 待导出的角色列表
     * @param response 响应
     * @throws IOException 异常
     */
    void download(List<RoleDto> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 获取用户权限列表
     *
     * @param user 用户信息
     * @return 权限列表
     */
    List<GrantedAuthority> mapToGrantedAuthorities(UserDto user);

    /**
     * 验证是否被用户关联
     *
     * @param ids 角色ID集合
     */
    void verification(Set<Long> ids);

    /**
     * 根据菜单Id集合查询角色
     *
     * @param menuIds 菜单ID集合
     * @return 角色列表
     */
    List<Role> findInMenuId(List<Long> menuIds);

}
