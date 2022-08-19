package com.cumulus.modules.business.detect.mapper;

import com.cumulus.base.BaseMapper;
import com.cumulus.modules.business.detect.dto.DetectRecordDto;
import com.cumulus.modules.business.detect.entity.DetectRecord;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 发现记录传输对象与发现记录实体的映射
 *
 * @author zhangxq
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DetectRecordMapper extends BaseMapper<DetectRecordDto, DetectRecord> {
}
