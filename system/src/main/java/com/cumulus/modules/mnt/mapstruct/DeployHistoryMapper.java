package com.cumulus.modules.mnt.mapstruct;

import com.cumulus.base.BaseMapper;
import com.cumulus.modules.mnt.entity.DeployHistory;
import com.cumulus.modules.mnt.dto.DeployHistoryDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 部署历史传输对象与部署历史实体的映射
 */
@Mapper(componentModel = "spring", uses = {}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeployHistoryMapper extends BaseMapper<DeployHistoryDto, DeployHistory> {

}
