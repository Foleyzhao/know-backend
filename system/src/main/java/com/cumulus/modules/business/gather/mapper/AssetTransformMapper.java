package com.cumulus.modules.business.gather.mapper;

import com.cumulus.base.BaseMapper;
import com.cumulus.modules.business.entity.Asset;
import com.cumulus.modules.business.entity.AssetTag;
import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.system.service.UserService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Asset 转 AssetEsMapper
 *
 * @author : shenjc
 */

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {Collectors.class, AssetTag.class})
public abstract class AssetTransformMapper implements BaseMapper<GatherAssetEs, Asset> {
    /**
     * 用户对象服务
     */
    @Autowired
    UserService userService;

    /**
     * 如果要使用需要设置 因为这两个对象的字段不同
     */
    @Override
    @Deprecated
    public Asset toEntity(GatherAssetEs dto) {
        return null;
    }

    /**
     * 将 Asset 对象转换为 AssetEs
     *
     * @param entity Asset对象
     * @return 返回AssetES
     */
    @Override
    @Mappings({
            @Mapping(target = "assetId", source = "id"),
            @Mapping(target = "ip", source = "ip"),
            @Mapping(target = "leader", expression = "java(entity.getDept() == null ? null : userService.getDeptHead(entity.getDept().getId()).getUsername())"),
            @Mapping(target = "name", source = "name"),
            @Mapping(target = "riskLevel", source = "riskLevel"),
            @Mapping(target = "dept", expression = "java(entity.getDept() == null ? null : entity.getDept().getName())"),
            @Mapping(target = "findTime", source = "createTime"),
            @Mapping(target = "assetType", expression = "java(entity.getAssetType() == null ? null : entity.getAssetType().getName())"),
            @Mapping(target = "updateTime", source = "updateTime"),
            @Mapping(target = "assetTags", expression = "java(entity.getAssetTags() == null ? new ArrayList<>() : entity.getAssetTags().stream().map(AssetTag::getName).collect(Collectors.toList()))"),
            @Mapping(target = "location", expression = "java(entity.getAssetExtend() == null ? null : entity.getAssetExtend().getLocation())"),
            @Mapping(target = "assetSysType", expression = "java(entity.getAssetSysType() == null ? null : entity.getAssetSysType().getId())"),
            @Mapping(target = "assetStatus", source = "assetStatus"),
            @Mapping(target = "port", expression = "java(entity.getPort() == null ? null : String.valueOf(entity.getPort()))"),
            @Mapping(target = "protocol", source = "protocol"),
            @Mapping(target = "headline", expression = "java(entity.getAssetExtend() == null ? null : entity.getAssetExtend().getTitle())"),
            @Mapping(target = "service", expression = "java(entity.getAssetExtend() == null ? null : entity.getAssetExtend().getServer())"),
            @Mapping(target = "serviceComponent", expression = "java(entity.getAssetExtend() == null ? null : entity.getAssetExtend().getServerComponent())"),
            @Mapping(target = "url", expression = "java(entity.getAssetExtend() == null ? null : entity.getAssetExtend().getWebsite())")
    })
    public abstract GatherAssetEs toDto(Asset entity);

    /**
     * 如果要使用需要设置 public Asset toEntity(AssetEs dto) 因为这两个对象的字段不同
     */
    @Override
    @Deprecated
    public List<Asset> toEntity(List<GatherAssetEs> dtoList) {
        return null;
    }
}
