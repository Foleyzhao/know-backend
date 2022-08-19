package com.cumulus.modules.system.service.impl;

import com.cumulus.config.FileProperties;
import com.cumulus.exception.BadRequestException;
import com.cumulus.exception.EntityExistException;
import com.cumulus.exception.EntityNotFoundException;
import com.cumulus.modules.security.service.UserCacheClean;
import com.cumulus.modules.system.dto.RoleDto;
import com.cumulus.modules.system.dto.SimpRoleDto;
import com.cumulus.modules.system.dto.SimpUserDto;
import com.cumulus.modules.system.dto.UserDto;
import com.cumulus.modules.system.dto.UserQueryCriteria;
import com.cumulus.modules.system.entity.Dept;
import com.cumulus.modules.system.entity.Job;
import com.cumulus.modules.system.entity.Role;
import com.cumulus.modules.system.entity.User;
import com.cumulus.modules.system.mapstruct.RoleMapper;
import com.cumulus.modules.system.mapstruct.SimpUserMapper;
import com.cumulus.modules.system.mapstruct.UserMapper;
import com.cumulus.modules.system.repository.DeptRepository;
import com.cumulus.modules.system.repository.RoleRepository;
import com.cumulus.modules.system.repository.UserRepository;
import com.cumulus.modules.system.service.DeptService;
import com.cumulus.modules.system.service.RoleService;
import com.cumulus.modules.system.service.UserService;
import com.cumulus.utils.CacheKey;
import com.cumulus.utils.FileUtils;
import com.cumulus.utils.QueryUtils;
import com.cumulus.utils.RedisUtils;
import com.cumulus.utils.SecurityUtils;
import org.apache.commons.lang3.StringUtils;
import com.cumulus.utils.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 系统用户服务实现
 *
 * @author shenjc
 */
@Service
@CacheConfig(cacheNames = "user")
public class UserServiceImpl implements UserService {

    /**
     * 系统用户数据访问接口
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * 系统用户传输对象与系统用户实体的映射
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * 用户密码加密器
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 系统文件属性配置
     */
    @Autowired
    private FileProperties properties;

    /**
     * redis工具类
     */
    @Autowired
    private RedisUtils redisUtils;

    /**
     * 用户缓存清理服务
     */
    @Autowired
    private UserCacheClean userCacheClean;

    /**
     * 部门数据层接口
     */
    @Autowired
    private DeptRepository deptRepository;

    /**
     * 角色 （权限组）数据层接口
     */
    @Autowired
    private RoleRepository roleRepository;

    /**
     * 权限组的Mapper
     */
    @Autowired
    private RoleMapper roleMapper;

    /**
     * 权限服务层
     */
    @Autowired
    private RoleService roleService;

    /**
     * 部门服务
     */
    @Autowired
    private DeptService deptService;

    @Override
    public Page<User> queryAll(UserQueryCriteria criteria, Pageable pageable) {
        return userRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
            criteriaQuery.distinct(true);
            return QueryUtils.getPredicate(root, criteria, criteriaBuilder);
        }, pageable);
    }

    @Override
    public List<UserDto> queryAll(UserQueryCriteria criteria) {
        List<User> users = userRepository.findAll((root, criteriaQuery, criteriaBuilder) ->
                QueryUtils.getPredicate(root, criteria, criteriaBuilder));
        return userMapper.toDto(users);
    }

    @Override
    @Cacheable(key = "'id:' + #p0")
    @Transactional(rollbackFor = Exception.class)
    public UserDto findById(Long id) {
        User user = userRepository.findById(id).orElseGet(User::new);
        ValidationUtils.isNull(user.getId(), "User", "id", id);
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(User resources) {
        checkCreate(resources);
        resources.setPassword(passwordEncoder.encode(User.DEFAULT_PWD));
        resources.setEnabled(resources.getEnabled() == null ? User.ENABLE : resources.getEnabled());
        resources.setGender(StringUtils.isBlank(resources.getGender()) ? User.DEFAULT_GENDER : resources.getGender());
        resources.setFirstLogin(User.FIRST_LOGIN);
        userRepository.save(resources);
        //更新权限组
        if (resources.getRoles() != null && !resources.getRoles().isEmpty()) {
            for (Role role : resources.getRoles()) {
                if (role == null || role.getId() == null) {
                    continue;
                }
                Optional<Role> roleOpt = roleRepository.findById(role.getId());
                if (!roleOpt.isPresent()) {
                    continue;
                }
                roleOpt.get().getUsers().add(resources);
                role.setUsers(roleOpt.get().getUsers());
                roleService.updateMenuAndUser(role, roleMapper.toDto(roleOpt.get()));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(User resources) {
        User oldUser = userRepository.findById(resources.getId()).orElseGet(User::new);
        checkUpdate(resources, oldUser);
        userRepository.save(oldUser);
        // 清除缓存
        delCaches(oldUser.getId(), oldUser.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCenter(User resources) {
        User user = userRepository.findById(resources.getId()).orElseGet(User::new);
        User user1 = userRepository.findByPhone(resources.getPhone());
        if (null != user1 && !user.getId().equals(user1.getId())) {
            throw new EntityExistException(User.class, "phone", resources.getPhone());
        }
        user.setNickName(resources.getNickName());
        user.setPhone(resources.getPhone());
        user.setGender(resources.getGender());
        userRepository.save(user);
        // 清理缓存
        delCaches(user.getId(), user.getUsername());
    }

    @Override
    public UserDto getDeptHead(Long deptId) {
        Dept dept = new Dept();
        dept.setId(deptId);
        Job job = new Job();
        job.setId(Job.DEFAULT_DEPT_HEAD_JOB_ID);
        List<User> userList = userRepository.findAllByDeptAndJob(dept, job);
        return userList.isEmpty() ? null : userMapper.toDto(userList.get(0));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPwd(Long userId) {
        final Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new BadRequestException("用户不存在");
        }
        checkLevel(userOpt.get());
        userRepository.updatePwdById(userId, passwordEncoder.encode(User.DEFAULT_PWD), new Date());
        delCaches(userOpt.get().getId(), userOpt.get().getUsername());
    }

    @Override
    public List<User> findAllByRoles(List<Long> roleIds) {
        return userRepository.findInRoleIds(roleIds);
    }

    @Override
    public List<UserDto> findByRoleId(Long roleId) {
        return userMapper.toDto(userRepository.findByRoleId(roleId));
    }

    @Override
    public List<User> findAllByDeptName(String deptName) {
        Optional<Dept> deptOpt = deptService.findByDeptName(deptName);
        List<User> users = new ArrayList<>();
        if (deptOpt.isPresent()){
            users = userRepository.findAllByDept(deptOpt.get());
        }
        return users;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        for (Long id : ids) {
            userRepository.findById(id).ifPresent(this::checkDelete);
        }
        for (Long id : ids) {
            // 清理缓存
            UserDto user = findById(id);
            delCaches(user.getId(), user.getUsername());
        }
        userRepository.deleteAllByIdIn(ids);
    }

    @Override
    public UserDto findByName(String userName) {
        User user = userRepository.findByUsername(userName);
        if (null == user) {
            throw new EntityNotFoundException(User.class, "name", userName);
        } else {
            return userMapper.toDto(user);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePass(String username, String pass) {
        userRepository.updatePwd(username, pass, new Date());
        flushCache(username);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> updateAvatar(MultipartFile multipartFile) {
        // 文件大小验证
        FileUtils.checkSize(properties.getAvatarMaxSize(), multipartFile.getSize());
        // 验证文件上传的格式
        String image = "gif jpg png jpeg";
        String fileType = FileUtils.getExtensionName(multipartFile.getOriginalFilename());
        if (null != fileType && !image.contains(fileType)) {
            throw new BadRequestException("文件格式错误！, 仅支持 " + image + " 格式");
        }
        User user = userRepository.findByUsername(SecurityUtils.getCurrentUsername());
        String oldPath = user.getAvatarPath();
        File file = FileUtils.upload(multipartFile, properties.getPath().getAvatar());
        user.setAvatarPath(Objects.requireNonNull(file).getPath());
        user.setAvatarName(file.getName());
        userRepository.save(user);
        if (StringUtils.isNotBlank(oldPath)) {
            FileUtils.del(oldPath);
        }
        @NotBlank String username = user.getUsername();
        flushCache(username);
        return new HashMap<String, String>(1) {

            private static final long serialVersionUID = -7188830365324257580L;

            {
                put("avatar", file.getName());
            }
        };
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEmail(String username, String email) {
        userRepository.updateEmail(username, email);
        flushCache(username);
    }

    @Override
    public void download(List<UserDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (UserDto userDTO : queryAll) {
            List<String> roles = userDTO.getRoles().stream().map(SimpRoleDto::getName).collect(Collectors.toList());
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("用户名", userDTO.getUsername());
            map.put("角色", roles);
            map.put("部门", userDTO.getDept().getName());
            map.put("岗位", userDTO.getJob());
            map.put("邮箱", userDTO.getEmail());
            map.put("状态", userDTO.getEnabled() ? "启用" : "禁用");
            map.put("手机号码", userDTO.getPhone());
            map.put("修改密码的时间", userDTO.getPwdResetTime());
            map.put("创建日期", userDTO.getCreateTime());
            list.add(map);
        }
        FileUtils.downloadExcel(list, response);
    }

    /**
     * 清理缓存
     *
     * @param id       用户ID
     * @param username 用户名
     */
    public void delCaches(Long id, String username) {
        redisUtils.del(CacheKey.USER_ID + id);
        flushCache(username);
    }

    /**
     * 清理缓存（登陆时）
     *
     * @param username 用户名
     */
    private void flushCache(String username) {
        userCacheClean.cleanUserCache(username);
    }

    /**
     * 校验删除的用户
     *
     * @param resources 用户对象
     */
    private void checkDelete(User resources) {
        checkLevel(resources);
        if (resources.getIsAdmin()) {
            throw new BadRequestException("角色权限不足");
        }
        if (Job.DEFAULT_DEPT_HEAD_JOB_ID == resources.getJob().getId()) {
            throw new BadRequestException("部门负责人无法删除");
        }
    }

    /**
     * 校验更新的用户名 邮箱 电话 去重, 不变的前端也需要发送过来
     *
     * @param newUser 用户
     * @param oldUser 旧用户
     */
    private void checkUpdate(User newUser, User oldUser) {
        checkLevel(newUser);
        if (StringUtils.isNotBlank(newUser.getEmail())) {
            oldUser.setEmail(newUser.getEmail());
        }
        if (StringUtils.isNotBlank(newUser.getPhone())) {
            oldUser.setPhone(newUser.getPhone());
        }
    }

    /**
     * 校验新增用户 用户名 邮箱 电话 去重,
     *
     * @param resources 用户
     */
    private void checkCreate(User resources) {
        checkLevel(resources);
        if (null != userRepository.findByUsername(resources.getUsername())) {
            throw new EntityExistException(User.class, "username", resources.getUsername());
        }
        if (resources.getDept().getId() == null || !deptRepository.findById(resources.getDept().getId()).isPresent()) {
            throw new BadRequestException("部门不存在");
        }
        if (resources.getRoles() != null) {
            for (Role role : resources.getRoles()) {
                if (role.getId() == null || !roleRepository.findById(role.getId()).isPresent()) {
                    throw new BadRequestException("权限组不存在");
                }
            }
        }
    }

    /**
     * 校验当前用户的角色级别是否低于创建用户的角色级别
     *
     * @param resources 用户
     */
    private void checkLevel(User resources) {
        if (resources.getRoles() == null) {
            return;
        }
        Integer currentLevel = Collections.min(roleService.findByUsersId(SecurityUtils.getCurrentUserId()).stream()
                .map(SimpRoleDto::getLevel).collect(Collectors.toList()));
        Integer optLevel = roleService.findByRoles(resources.getRoles());
        if (currentLevel > optLevel) {
            throw new BadRequestException("角色权限不足");
        }
    }
}
