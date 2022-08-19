package com.cumulus.modules.system.service;

import com.cumulus.modules.system.dto.UserDto;

import java.util.List;

/**
 * 数据权限服务接口
 */
public interface DataService {

    /**
     * 获取数据权限
     *
     * @param user 用户
     * @return 数据权限列表
     */
    List<Long> getDeptIds(UserDto user);

}
