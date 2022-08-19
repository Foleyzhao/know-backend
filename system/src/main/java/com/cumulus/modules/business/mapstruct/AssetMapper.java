package com.cumulus.modules.business.mapstruct;

import com.cumulus.base.BaseMapper;
import com.cumulus.modules.business.dto.AssetDto;
import com.cumulus.modules.business.entity.Asset;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 资产传输对象与资产实体的映射
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AssetMapper extends BaseMapper<AssetDto, Asset> {
}
