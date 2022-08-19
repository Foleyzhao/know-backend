package com.cumulus.modules.system.mapstruct;

import com.cumulus.base.BaseMapper;
import com.cumulus.modules.system.entity.Role;
import com.cumulus.modules.system.dto.RoleDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 系统角色传输对象与系统角色实体的映射
 *
 * @author shenjc
 */
@Mapper(componentModel = "spring", uses = {SimpUserMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper extends BaseMapper<RoleDto, Role> {
}
