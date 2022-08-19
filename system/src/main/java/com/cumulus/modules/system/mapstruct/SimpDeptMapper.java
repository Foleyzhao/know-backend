package com.cumulus.modules.system.mapstruct;

import com.cumulus.base.BaseMapper;
import com.cumulus.modules.system.dto.SimpDeptDto;
import com.cumulus.modules.system.entity.Dept;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 精简的系统部门传输对象与系统部门实体的映射
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SimpDeptMapper extends BaseMapper<SimpDeptDto, Dept> {
}
