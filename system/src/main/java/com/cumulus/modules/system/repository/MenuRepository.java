package com.cumulus.modules.system.repository;

import com.cumulus.modules.system.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

/**
 * 系统菜单数据访问接口
 */
public interface MenuRepository extends JpaRepository<Menu, Long>, JpaSpecificationExecutor<Menu> {

    /**
     * 根据菜单标题查询菜单
     *
     * @param title 菜单标题
     * @return 菜单
     */
    Menu findByTitle(String title);

    /**
     * 根据组件名称查询菜单
     *
     * @param name 组件名称
     * @return 菜单
     */
    Menu findByComponentName(String name);

    /**
     * 根据父菜单ID查询菜单列表
     *
     * @param pid 父菜单ID
     * @return 子菜单列表
     */
    List<Menu> findByPid(Long pid);

    /**
     * 查询顶级菜单
     *
     * @return 顶级菜单列表
     */
    List<Menu> findByPidIsNull();

    /**
     * 根据角色ID集合与非菜单类型查询菜单
     *
     * @param roleIds 角色ID集合
     * @return 菜单集合
     */
    @Query(value = "SELECT m.* FROM sys_menu m, sys_roles_menus rm WHERE m.id = rm.menu_id AND rm.role_id IN ?1 " +
            "order by m.menu_sort asc", nativeQuery = true)
    Set<Menu> findByRoleIds(Set<Long> roleIds);

    /**
     * 根据菜单ID获取子菜单数量
     *
     * @param id 菜单ID
     * @return 子菜单数量
     */
    int countByPid(Long id);

    /**
     * 更新子菜单数量
     *
     * @param count  子菜单数量
     * @param menuId 菜单ID
     */
    @Modifying
    @Query(value = " update sys_menu set sub_count = ?1 where id = ?2 ", nativeQuery = true)
    void updateSubCntById(int count, Long menuId);

    /**
     * 查询全部权限
     *
     * @param hidden 是否隐藏
     * @return 返回列表
     */
    List<Menu> findAllByHidden(Boolean hidden);
}
