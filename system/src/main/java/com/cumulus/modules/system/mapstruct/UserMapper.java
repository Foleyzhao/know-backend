package com.cumulus.modules.system.mapstruct;

import com.cumulus.base.BaseMapper;
import com.cumulus.modules.system.entity.User;
import com.cumulus.modules.system.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 系统用户传输对象与系统用户实体的映射
 */
@Mapper(componentModel = "spring", uses = {RoleMapper.class, DeptMapper.class, JobMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper extends BaseMapper<UserDto, User> {
}
