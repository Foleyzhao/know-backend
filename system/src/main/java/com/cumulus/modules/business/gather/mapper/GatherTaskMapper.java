package com.cumulus.modules.business.gather.mapper;

import com.cumulus.base.BaseMapper;
import com.cumulus.modules.business.gather.dto.GatherTaskDto;
import com.cumulus.modules.business.gather.entity.mysql.GatherPlan;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 采集任务传输对象与采集任务实体的映射
 *
 * @author zhangxq
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GatherTaskMapper extends BaseMapper<GatherTaskDto, GatherPlan> {
}
