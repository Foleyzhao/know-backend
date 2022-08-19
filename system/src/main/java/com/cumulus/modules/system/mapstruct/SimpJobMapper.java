package com.cumulus.modules.system.mapstruct;

import com.cumulus.base.BaseMapper;
import com.cumulus.modules.system.entity.Job;
import com.cumulus.modules.system.dto.SimpJobDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 精简的系统岗位传输对象与系统岗位实体的映射
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SimpJobMapper extends BaseMapper<SimpJobDto, Job> {
}
