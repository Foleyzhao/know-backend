package com.cumulus.modules.business.mapstruct;

import com.cumulus.base.BaseMapper;
import com.cumulus.modules.business.dto.AssetTagListTreeDto;
import com.cumulus.modules.business.dto.AssetTagTreeDto;
import com.cumulus.modules.business.entity.AssetTag;
import com.cumulus.modules.business.repository.AssetTagRepository;
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
public abstract class AssetTagListTreeMapper implements BaseMapper<AssetTagListTreeDto, AssetTag> {

    @Autowired
    @Lazy
    protected AssetTagRepository assetTagRepository;

    /**
     * 将实体转换为
     *
     * @param entity 实体类
     * @return 返回DTO
     */
    @Override
    @Mapping(target = "subTagList", expression = "java(assetTagRepository.findAllByParentAndEnabledIsTrue(entity))")
    public abstract AssetTagListTreeDto toDto(AssetTag entity);
}
