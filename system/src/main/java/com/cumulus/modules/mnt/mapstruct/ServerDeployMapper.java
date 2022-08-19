package com.cumulus.modules.mnt.mapstruct;

import com.cumulus.base.BaseMapper;
import com.cumulus.modules.mnt.entity.ServerDeploy;
import com.cumulus.modules.mnt.dto.ServerDeployDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 服务器传输对象与服务器实体的映射
 */
@Mapper(componentModel = "spring", uses = {}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ServerDeployMapper extends BaseMapper<ServerDeployDto, ServerDeploy> {

}
