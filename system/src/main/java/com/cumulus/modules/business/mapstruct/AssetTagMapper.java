package com.cumulus.modules.business.mapstruct;

import com.cumulus.base.BaseMapper;
import com.cumulus.modules.business.dto.AssetTagDto;
import com.cumulus.modules.business.entity.AssetTag;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 资产标签传输对象与资产标签实体的映射
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AssetTagMapper extends BaseMapper<AssetTagDto, AssetTag> {

}
