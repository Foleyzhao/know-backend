package com.cumulus.modules.system.repository;

import com.cumulus.modules.system.entity.Dept;
import com.cumulus.modules.system.entity.Job;
import com.cumulus.modules.system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 系统用户数据访问接口
 *
 * @author shenjc
 */
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户
     */
    User findByUsername(String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户
     */
    User findByEmail(String email);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户
     */
    User findByPhone(String phone);

    /**
     * 修改密码
     *
     * @param username              用户名
     * @param pass                  密码
     * @param lastPasswordResetTime 最后一次修改密码时间
     */
    @Modifying
    @Query(value = "update sys_user set password = ?2 , pwd_reset_time = ?3, first_login = 0 where username = ?1", nativeQuery = true)
    void updatePwd(String username, String pass, Date lastPasswordResetTime);

    /**
     * 修改密码
     *
     * @param userId                用户id
     * @param pass                  密码
     * @param lastPasswordResetTime 最后一次修改密码时间
     */
    @Modifying
    @Query(value = "update sys_user set password = ?2, pwd_reset_time = ?3, first_login = 1 where id = ?1", nativeQuery = true)
    void updatePwdById(Long userId, String pass, Date lastPasswordResetTime);

    /**
     * 修改邮箱
     *
     * @param username 用户名
     * @param email    邮箱
     */
    @Modifying
    @Query(value = "update sys_user set email = ?2 where username = ?1", nativeQuery = true)
    void updateEmail(String username, String email);

    /**
     * 根据角色查询用户
     *
     * @param roleId 角色ID
     * @return 用户列表
     */
    @Query(value = "SELECT u.* FROM sys_user u, sys_users_roles ur WHERE u.id = ur.user_id AND ur.role_id = ?1",
            nativeQuery = true)
    List<User> findByRoleId(Long roleId);

    /**
     * 根据部门查询用户
     *
     * @param dept 部门 实际只需要部门id
     * @return 用户列表
     */
    List<User> findAllByDept(Dept dept);

    /**
     * 根据菜单查询用户
     *
     * @param id 菜单ID
     * @return 用户列表
     */
    @Query(value = "SELECT u.* FROM sys_user u, sys_users_roles ur, sys_roles_menus rm WHERE u.id = ur.user_id " +
            "AND ur.role_id = rm.role_id AND rm.menu_id = ?1 group by u.id", nativeQuery = true)
    List<User> findByMenuId(Long id);

    /**
     * 根据菜单查询用户
     *
     * @param deptName 部门名称
     * @param menuId   菜单ID
     * @return 用户列表
     */
    @Query(value = "select * from sys_user su INNER JOIN " +
            "(SELECT DISTINCT sur.user_id FROM sys_users_roles sur, sys_roles_menus srm where sur.role_id = srm.role_id and srm.menu_id = ?1) role " +
            "on su.id = role.user_id " +
            "and su.dept_id = (select id from sys_dept where `name` = ?2 LIMIT 1);", nativeQuery = true)
    List<User> findByMenuIdAndDeptName(Long menuId, String deptName);

    /**
     * 根据Id删除用户
     *
     * @param ids 用户ID列表
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * 根据岗位统计用户数量
     *
     * @param ids 岗位ID集合
     * @return 用户数量
     */
    @Query(value = "SELECT count(1) FROM sys_user u, sys_users_jobs uj WHERE u.id = uj.user_id AND uj.job_id IN ?1",
            nativeQuery = true)
    int countByJobs(Set<Long> ids);

    /**
     * 根据部门统计用户数量
     *
     * @param deptIds 部门ID列表
     * @return 用户数量
     */
    @Query(value = "SELECT count(1) FROM sys_user u WHERE u.dept_id IN ?1 AND u.job_id NOT IN ?2", nativeQuery = true)
    int countByDepts(Set<Long> deptIds, Set<Long> jobId);

    /**
     * 根据角色统计用户数量
     *
     * @param ids 角色ID列表
     * @return 用户数量
     */
    @Query(value = "SELECT count(1) FROM sys_user u, sys_users_roles ur WHERE u.id = ur.user_id AND ur.role_id in ?1",
            nativeQuery = true)
    int countByRoles(Set<Long> ids);

    /**
     * 根据部门和岗位寻找用户
     *
     * @param dept 部门信息
     * @param job  用户信息
     * @return 返回用户列表
     */
    List<User> findAllByDeptAndJob(Dept dept, Job job);

    /**
     * 删除部门管理员
     *
     * @param dept 部门信息 （只需要id）
     */
    void deleteByDept(Dept dept);

    /**
     * 根据权限组列表查询用户
     *
     * @param roleIds 权限组列表
     * @return 返回用户列表
     */
    @Query(value = "SELECT DISTINCT u.* FROM sys_user u, sys_users_roles ur WHERE u.id = ur.user_id AND (ur.role_id in ?1 or u.is_admin = 1)",
            nativeQuery = true)
    List<User> findInRoleIds(List<Long> roleIds);
}
