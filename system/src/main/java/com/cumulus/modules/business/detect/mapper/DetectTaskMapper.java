package com.cumulus.modules.business.detect.mapper;

import com.cumulus.base.BaseMapper;
import com.cumulus.modules.business.detect.dto.DetectTaskDto;
import com.cumulus.modules.business.detect.entity.DetectTask;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 发现任务传输对象与发现任务实体的映射
 *
 * @author zhangxq
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DetectTaskMapper extends BaseMapper<DetectTaskDto, DetectTask> {
}
