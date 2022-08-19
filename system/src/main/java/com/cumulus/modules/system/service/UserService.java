package com.cumulus.modules.system.service;

import com.cumulus.modules.system.entity.User;
import com.cumulus.modules.system.dto.UserDto;
import com.cumulus.modules.system.dto.UserQueryCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 系统用户服务接口
 *
 * @author shenjc
 */
public interface UserService {

    /**
     * 根据用户ID查询用户
     *
     * @param id 用户ID
     * @return 用户
     */
    UserDto findById(Long id);

    /**
     * 新增用户
     *
     * @param resources 用户
     */
    void create(User resources);

    /**
     * 编辑用户
     *
     * @param resources 用户
     */
    void update(User resources);

    /**
     * 删除用户
     *
     * @param ids 用户ID集合
     */
    void delete(Set<Long> ids);

    /**
     * 根据用户名查询
     *
     * @param userName 用户名
     * @return 用户
     */
    UserDto findByName(String userName);

    /**
     * 修改密码
     *
     * @param username        用户名
     * @param encryptPassword 密码
     */
    void updatePass(String username, String encryptPassword);

    /**
     * 修改头像
     *
     * @param file 头像文件
     * @return 头像信息
     */
    Map<String, String> updateAvatar(MultipartFile file);

    /**
     * 修改邮箱
     *
     * @param username 用户名
     * @param email    邮箱
     */
    void updateEmail(String username, String email);

    /**
     * 根据条件查询用户
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return 用户列表
     */
    Page<User> queryAll(UserQueryCriteria criteria, Pageable pageable);

    /**
     * 查询全部用户
     *
     * @param criteria 查询参数
     * @return 用户列表
     */
    List<UserDto> queryAll(UserQueryCriteria criteria);

    /**
     * 导出用户列表
     *
     * @param queryAll 待导出的用户列表
     * @param response 响应
     * @throws IOException 异常
     */
    void download(List<UserDto> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 修改个人信息
     *
     * @param resources 用户
     */
    void updateCenter(User resources);

    /**
     * 获取部门负责人
     *
     * @param deptId 部门id
     * @return 返回用户信息
     */
    UserDto getDeptHead(Long deptId);

    /**
     * 重置密码
     *
     * @param userId 用户id
     */
    void resetPwd(Long userId);

    /**
     * 根据权限组列表查询用户
     *
     * @param roles 权限组列表
     * @return 返回用户列表
     */
    List<User> findAllByRoles(List<Long> roles);

    /**
     * 根据角色查询用户
     *
     * @param roleId 角色ID
     * @return 用户列表
     */
    List<UserDto> findByRoleId(Long roleId);

    /**
     * 根据部门名获取用户列表
     *
     * @param deptName 部门名
     * @return 用户列表
     */
    List<User> findAllByDeptName(String deptName);
}
