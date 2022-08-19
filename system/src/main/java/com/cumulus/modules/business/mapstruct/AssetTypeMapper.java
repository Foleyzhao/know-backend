package com.cumulus.modules.business.mapstruct;

import com.cumulus.base.BaseMapper;
import com.cumulus.modules.business.dto.AssetTypeDto;
import com.cumulus.modules.business.entity.AssetType;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 资产类型传输对象与资产类型实体的映射
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AssetTypeMapper extends BaseMapper<AssetTypeDto, AssetType> {
}
