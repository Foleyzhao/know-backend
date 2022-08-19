package com.cumulus.modules.business.mapstruct;

import com.cumulus.base.BaseMapper;
import com.cumulus.modules.business.dto.IpLibraryDto;
import com.cumulus.modules.business.entity.IpLibrary;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * ip库传输对象与ip库的映射
 *
 * @author zhangxq
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IpLibraryMapper extends BaseMapper<IpLibraryDto, IpLibrary> {
}
