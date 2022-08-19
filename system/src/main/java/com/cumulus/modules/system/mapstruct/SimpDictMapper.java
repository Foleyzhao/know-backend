package com.cumulus.modules.system.mapstruct;

import com.cumulus.base.BaseMapper;
import com.cumulus.modules.system.entity.Dict;
import com.cumulus.modules.system.dto.SimpDictDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 精简的系统字典传输对象与系统字典实体的映射
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SimpDictMapper extends BaseMapper<SimpDictDto, Dict> {
}
