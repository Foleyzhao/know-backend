package com.cumulus.modules.system.repository;

import com.cumulus.modules.system.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

/**
 * 系统角色数据访问接口
 */
public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {

    /**
     * 根据角色名称查询
     *
     * @param name 角色名称
     * @return 角色
     */
    Role findByName(String name);

    /**
     * 根据角色ID集合删除角色
     *
     * @param ids 角色ID集合
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * 根据用户ID查询角色
     *
     * @param id 用户ID
     * @return 角色列表
     */
    @Query(value = "SELECT r.* FROM sys_role r, sys_users_roles ur WHERE r.id = ur.role_id AND ur.user_id = ?1",
            nativeQuery = true)
    Set<Role> findByUserId(Long id);

    /**
     * 根据菜单ID解除该菜单的所有角色绑定
     *
     * @param id 菜单ID
     */
    @Modifying
    @Query(value = "delete from sys_roles_menus where menu_id = ?1", nativeQuery = true)
    void untiedMenu(Long id);

    /**
     * 根据菜单Id集合查询角色
     *
     * @param menuIds 菜单ID集合
     * @return 角色列表
     */
    @Query(value = "SELECT r.* FROM sys_role r, sys_roles_menus rm WHERE r.id = rm.role_id AND rm.menu_id in ?1",
            nativeQuery = true)
    List<Role> findInMenuId(List<Long> menuIds);

}
