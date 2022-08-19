package com.cumulus.modules.business.mapstruct;

import com.cumulus.base.BaseMapper;
import com.cumulus.modules.business.dto.AssetTagTreeDto;
import com.cumulus.modules.business.entity.AssetTag;
import com.cumulus.modules.business.service.AssetTagService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;

/**
 * 资产标签树形结构Mapper
 *
 * @author : shenjc
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {PageRequest.class})
public abstract class AssetTagTreeMapper implements BaseMapper<AssetTagTreeDto, AssetTag> {

    @Autowired
    @Lazy
    protected AssetTagService assetTagService;

    /**
     * 将实体转换为
     *
     * @param entity 实体类
     * @return 返回DTO
     */
    @Override
    @Mapping(target = "subTagPage", expression = "java(assetTagService.queryTreePageSub(entity.getId(), PageRequest.of(0, 10)))")
    public abstract AssetTagTreeDto toDto(AssetTag entity);
}
