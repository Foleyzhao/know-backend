package com.cumulus.modules.business.mapstruct;

import com.cumulus.base.BaseMapper;
import com.cumulus.modules.business.dto.AssetWarehouseDto;
import com.cumulus.modules.business.entity.Asset;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 资产仓库DtoMapper
 *
 * @author : shenjc
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AssetWareHouseMapper extends BaseMapper<AssetWarehouseDto, Asset> {
}
