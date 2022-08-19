package com.cumulus.mapstruct;

import com.cumulus.base.BaseMapper;
import com.cumulus.entity.LocalStorage;
import com.cumulus.dto.LocalStorageDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 本地存储实体与本地存储传输对象映射
 *
 * @author zhaoff
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LocalStorageMapper extends BaseMapper<LocalStorageDto, LocalStorage> {

}
