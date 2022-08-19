package com.cumulus.modules.system.mapstruct;

import com.cumulus.base.BaseMapper;
import com.cumulus.modules.system.entity.Role;
import com.cumulus.modules.system.dto.SimpRoleDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 精简的系统角色传输对象与系统角色实体的映射
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SimpRoleMapper extends BaseMapper<SimpRoleDto, Role> {
}
