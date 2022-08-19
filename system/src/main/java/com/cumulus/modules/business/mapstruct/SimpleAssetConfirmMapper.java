package com.cumulus.modules.business.mapstruct;

import com.cumulus.base.BaseMapper;
import com.cumulus.modules.business.dto.SimpleAssetConfirmDto;
import com.cumulus.modules.business.entity.AssetConfirm;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @author zhangxq
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SimpleAssetConfirmMapper extends BaseMapper<SimpleAssetConfirmDto, AssetConfirm> {
}
