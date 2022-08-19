package com.cumulus.modules.system.mapstruct;

import com.cumulus.base.BaseMapper;
import com.cumulus.enums.DetailedFileTypeEnum;
import com.cumulus.modules.system.dto.DetailedFileDto;
import com.cumulus.modules.system.entity.DetailedFile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

/**
 * 明细清单对象Mapper
 *
 * @author : shenjc
 */
@Mapper(componentModel = "spring", imports = {DetailedFileTypeEnum.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DetailedFileMapper extends BaseMapper<DetailedFileDto, DetailedFile> {

    /**
     * 实体转换为dto
     *
     * @param entity 实体
     * @return 返回dto
     */
    @Override
    @Mappings({
            @Mapping(target = "type", expression = "java(DetailedFileTypeEnum.getNameByType(entity.getType()))"),
            @Mapping(target = "status", expression = "java(entity.statusStr())")
    })
    DetailedFileDto toDto(DetailedFile entity);
}
