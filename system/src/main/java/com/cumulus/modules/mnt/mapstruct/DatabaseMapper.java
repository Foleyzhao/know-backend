package com.cumulus.modules.mnt.mapstruct;

import com.cumulus.base.BaseMapper;
import com.cumulus.modules.mnt.entity.Database;
import com.cumulus.modules.mnt.dto.DatabaseDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 数据库传输对象与数据库实体的映射
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DatabaseMapper extends BaseMapper<DatabaseDto, Database> {

}
