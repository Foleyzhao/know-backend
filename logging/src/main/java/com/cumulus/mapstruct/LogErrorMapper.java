package com.cumulus.mapstruct;

import com.cumulus.base.BaseMapper;
import com.cumulus.entity.Log;
import com.cumulus.dto.ErrorLogDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * ERROR类型的操作日志传输对象与操作日志实体的映射
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LogErrorMapper extends BaseMapper<ErrorLogDTO, Log> {

}
