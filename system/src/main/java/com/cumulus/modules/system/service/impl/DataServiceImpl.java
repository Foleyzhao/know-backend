package com.cumulus.modules.system.service.impl;

import com.cumulus.enums.DataScopeEnum;
import com.cumulus.modules.system.dto.SimpRoleDto;
import com.cumulus.modules.system.dto.UserDto;
import com.cumulus.modules.system.entity.Dept;
import com.cumulus.modules.system.service.DataService;
import com.cumulus.modules.system.service.DeptService;
import com.cumulus.modules.system.service.RoleService;
import com.cumulus.modules.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 数据权限服务实现
 */
@Service
@CacheConfig(cacheNames = "data")
public class DataServiceImpl implements DataService {

    /**
     * 用户服务
     */
    @Autowired
    private UserService userService;

    /**
     * 系统角色服务接口
     */
    @Autowired
    private RoleService roleService;

    /**
     * 系统部门服务接口
     */
    @Autowired
    private DeptService deptService;

    /**
     * 根据用户获取数据权限允许访问的部门ID列表
     *
     * @param user 用户
     * @return 部门ID列表
     */
    @Override
    @Cacheable(key = "'user:' + #p0.id")
    public List<Long> getDeptIds(UserDto user) {
        // 用于存储部门id
        Set<Long> deptIds = new HashSet<>();
        // 查询用户角色
        List<SimpRoleDto> roleSet = roleService.findByUsersId(user.getId());
        // 获取对应的部门ID
        for (SimpRoleDto role : roleSet) {
            DataScopeEnum dataScopeEnum = DataScopeEnum.find(role.getDataScope());
            switch (Objects.requireNonNull(dataScopeEnum)) {
                case THIS_LEVEL:
                    deptIds.add(user.getDept().getId());
                    break;
                case THIS_LEVEL_AND_BELOW:
                    deptIds.addAll(getThisLeveAndBelow(user.getId()));
                    break;
                default:
                    return new ArrayList<>(deptIds);
            }
        }
        return new ArrayList<>(deptIds);
    }

    /**
     * 获取自定义的数据权限
     *
     * @param userId 用户id
     * @return 数据权限ID
     */
    public Set<Long> getThisLeveAndBelow(Long userId) {
        Set<Long> deptIds = new HashSet<>();
        UserDto userDto = userService.findById(userId);
        if (userDto != null) {
            deptIds.add(userDto.getDept().getId());
            List<Dept> deptChildren = deptService.findByPid(userDto.getDept().getId());
            if (deptChildren != null && !deptChildren.isEmpty()) {
                deptIds.addAll(deptService.getDeptChildren(deptChildren));
            }
        }
        return deptIds;
    }
}
