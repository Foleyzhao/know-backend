package com.cumulus.modules.mnt.mapstruct;

import com.cumulus.base.BaseMapper;
import com.cumulus.modules.mnt.entity.Deploy;
import com.cumulus.modules.mnt.dto.DeployDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 部署传输对象与部署实体的映射
 */
@Mapper(componentModel = "spring", uses = {AppMapper.class, ServerDeployMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeployMapper extends BaseMapper<DeployDto, Deploy> {

}
