package com.cumulus.modules.mnt.mapstruct;

import com.cumulus.base.BaseMapper;
import com.cumulus.modules.mnt.entity.App;
import com.cumulus.modules.mnt.dto.AppDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 应用传输对象与应用实体的映射
 */
@Mapper(componentModel = "spring", uses = {}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AppMapper extends BaseMapper<AppDto, App> {

}
