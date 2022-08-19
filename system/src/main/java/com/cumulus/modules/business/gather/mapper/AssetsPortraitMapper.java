package com.cumulus.modules.business.gather.mapper;

import com.cumulus.base.BaseMapper;
import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.vo.AssetPortraitVo;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 采集传输对象与资产画像做实体的映射
 *
 * @author shijh
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AssetsPortraitMapper extends BaseMapper<AssetPortraitVo, GatherAssetEs> {
}
