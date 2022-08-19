package com.cumulus.modules.system.mapstruct;

import com.cumulus.base.BaseMapper;
import com.cumulus.modules.system.entity.DictDetail;
import com.cumulus.modules.system.dto.DictDetailDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 系统字典详情传输对象与系统字典详情实体的映射
 */
@Mapper(componentModel = "spring", uses = {SimpDictMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DictDetailMapper extends BaseMapper<DictDetailDto, DictDetail> {
}
