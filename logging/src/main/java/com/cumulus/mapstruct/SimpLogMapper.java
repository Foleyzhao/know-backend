package com.cumulus.mapstruct;

import com.cumulus.base.BaseMapper;
import com.cumulus.dto.SimpLogDTO;
import com.cumulus.entity.Log;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * 精简操作日志传输对象与操作日志实体的映射
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SimpLogMapper extends BaseMapper<SimpLogDTO, Log> {

    @Override
    @Mapping(target = "logType", expression = "java(com.cumulus.enums.LogTypeEnum.valueToDescription(entity.getLogType()))")
    SimpLogDTO toDto(Log entity);
}
