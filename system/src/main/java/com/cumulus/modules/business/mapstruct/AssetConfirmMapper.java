package com.cumulus.modules.business.mapstruct;

import com.cumulus.base.BaseMapper;
import com.cumulus.modules.business.dto.AssetConfirmDto;
import com.cumulus.modules.business.entity.AssetConfirm;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 确认资产传输对象与确认资产实体对象的映射
 *
 * @author zhangxq
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AssetConfirmMapper extends BaseMapper<AssetConfirmDto, AssetConfirm> {
}
