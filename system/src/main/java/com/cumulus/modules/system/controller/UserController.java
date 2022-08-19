package com.cumulus.modules.system.controller;

import com.cumulus.base.BaseEntity;
import com.cumulus.config.RsaProperties;
import com.cumulus.enums.CodeEnum;
import com.cumulus.exception.BadRequestException;
import com.cumulus.modules.system.dto.SimpRoleDto;
import com.cumulus.modules.system.dto.UserDto;
import com.cumulus.modules.system.dto.UserQueryCriteria;
import com.cumulus.modules.system.entity.Job;
import com.cumulus.modules.system.entity.User;
import com.cumulus.modules.system.service.RoleService;
import com.cumulus.modules.system.service.UserService;
import com.cumulus.modules.system.service.VerifyService;
import com.cumulus.modules.system.vo.UserPassVo;
import com.cumulus.utils.RegexUtil;
import com.cumulus.utils.RsaUtils;
import com.cumulus.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户控制层
 *
 * @author shenjc
 */
@RestController
@RequestMapping("/api/users")
@PreAuthorize("@auth.check('organizationalManagement')")
public class UserController {

    /**
     * 用户密码加密器
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户服务接口
     */
    @Autowired
    private UserService userService;

    /**
     * 角色服务接口
     */
    @Autowired
    private RoleService roleService;

    /**
     * 验证码服务接口
     */
    @Autowired
    private VerifyService verificationCodeService;

    /**
     * 查询用户 目前只支持 单个部门查询 或者全部部门查询
     *
     * @param criteria 查询参数 实际只要 deptId
     * @param pageable 分页参数
     * @return 用户列表
     */
    @GetMapping
    public ResponseEntity<Object> query(UserQueryCriteria criteria, Pageable pageable) {
        //如果部门id 不为空则放入 criteria 的部门列表中
        if (criteria.getDeptId() != null) {
            criteria.setDeptIds(new HashSet<>(Collections.singletonList(criteria.getDeptId())));
        }
        return new ResponseEntity<>(userService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    /**
     * 新增用户
     *
     * @param resources 用户
     * @return 响应
     */
    @PostMapping
    public ResponseEntity<Object> create(@Validated(BaseEntity.Create.class) @RequestBody User resources) {
        Job job = new Job();
        job.setId(Job.DEFAULT_NEW_USER_JOB_ID);
        resources.setJob(job);
        userService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 修改用户
     *
     * @param resources 用户
     * @return 响应
     */
    @PutMapping
    public ResponseEntity<Object> update(@Validated(User.Update.class) @RequestBody User resources) {
        userService.update(resources);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 删除用户
     *
     * @param ids 用户ID结集合
     * @return 响应
     */
    @DeleteMapping
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        for (Long id : ids) {
            final List<Integer> currentUserRoles = roleService.findByUsersId(SecurityUtils.getCurrentUserId()).stream()
                    .map(SimpRoleDto::getLevel).collect(Collectors.toList());
            if (currentUserRoles.isEmpty()) {
                throw new BadRequestException("角色权限不足，不能删除：" + userService.findById(id).getUsername());
            }
            final List<Integer> userRoles = roleService.findByUsersId(id).stream().map(SimpRoleDto::getLevel)
                    .collect(Collectors.toList());
            if (!userRoles.isEmpty()) {
                Integer currentLevel = Collections.min(currentUserRoles);
                Integer optLevel = Collections.min(userRoles);
                if (currentLevel > optLevel) {
                    throw new BadRequestException("角色权限不足，不能删除：" + userService.findById(id).getUsername());
                }
            }
        }
        userService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 修改密码
     *
     * @param passVo 修改密码表单
     * @return 响应
     * @throws Exception 异常
     */
    @PostMapping(value = "/updatePass")
    @PreAuthorize("@auth.check(@auth.NO_PERMISSION)")
    public ResponseEntity<Object> updatePass(@RequestBody UserPassVo passVo) throws Exception {
        String oldPass = RsaUtils.decryptByPrivateKey(RsaProperties.privateKey, passVo.getOldPass());
        String newPass = RsaUtils.decryptByPrivateKey(RsaProperties.privateKey, passVo.getNewPass());
        UserDto user = userService.findByName(SecurityUtils.getCurrentUsername());
        if (!passwordEncoder.matches(oldPass, user.getPassword())) {
            throw new BadRequestException("修改失败，旧密码错误");
        }
        if (passwordEncoder.matches(newPass, user.getPassword())) {
            throw new BadRequestException("新密码不能与旧密码相同");
        }
        if (User.DEFAULT_PWD.equals(newPass)) {
            throw new BadRequestException("修改失败,新密码不能和默认密码相同");
        }
        if (!RegexUtil.regexRegular(RegexUtil.DEFAULT_PWD_PATTERN, newPass)) {
            throw new BadRequestException("密码格式不符合");
        }
        userService.updatePass(user.getUsername(), passwordEncoder.encode(newPass));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 重置密码
     *
     * @param userId 用户id
     * @return 返回信息
     */
    @PostMapping("resetPwd")
    public ResponseEntity<?> resetPwd(Long userId) {
        userService.resetPwd(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 根据角色查询用户
     *
     * @param roleId 角色ID
     * @return 用户列表
     */
    @PostMapping("findByRoleId")
    public ResponseEntity<?> findByRoleId(Long roleId) {
        return new ResponseEntity<>(userService.findByRoleId(roleId), HttpStatus.OK);
    }

    /**
     * 获取部门负责人
     *
     * @param deptId 部门id
     * @return 用户列表
     */
    @PostMapping("getDeptHead")
    public ResponseEntity<?> getDeptHead(Long deptId) {
        return new ResponseEntity<>(userService.getDeptHead(deptId), HttpStatus.OK);
    }

    /**
     * 导出用户数据
     *
     * @param response 响应
     * @param criteria 查询参数
     * @throws IOException 异常
     */
    @GetMapping(value = "/download")
    public void download(HttpServletResponse response, UserQueryCriteria criteria) throws IOException {
        userService.download(userService.queryAll(criteria), response);
    }

    /**
     * 修改用户信息（个人中心）
     *
     * @param resources 用户
     * @return 响应
     */
    @PutMapping(value = "center")
    @PreAuthorize("@auth.check(@auth.NO_PERMISSION)")
    public ResponseEntity<Object> center(@Validated(User.Update.class) @RequestBody User resources) {
        if (!resources.getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BadRequestException("不能修改他人资料");
        }
        userService.updateCenter(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 修改头像
     *
     * @param avatar 头像
     * @return 头像信息
     */
    @PostMapping(value = "/updateAvatar")
    public ResponseEntity<Object> updateAvatar(@RequestParam MultipartFile avatar) {
        return new ResponseEntity<>(userService.updateAvatar(avatar), HttpStatus.OK);
    }

    /**
     * 修改邮箱
     *
     * @param code 验证码
     * @param user 用户
     * @return 响应
     * @throws Exception 异常
     */
    @PostMapping(value = "/updateEmail/{code}")
    public ResponseEntity<Object> updateEmail(@PathVariable String code, @RequestBody User user) throws Exception {
        String password = RsaUtils.decryptByPrivateKey(RsaProperties.privateKey, user.getPassword());
        UserDto userDto = userService.findByName(SecurityUtils.getCurrentUsername());
        if (!passwordEncoder.matches(password, userDto.getPassword())) {
            throw new BadRequestException("密码错误");
        }
        verificationCodeService.validated(CodeEnum.EMAIL_RESET_EMAIL_CODE.getKey() + user.getEmail(), code);
        userService.updateEmail(userDto.getUsername(), user.getEmail());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
