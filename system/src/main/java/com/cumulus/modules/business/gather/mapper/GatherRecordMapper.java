package com.cumulus.modules.business.gather.mapper;

import com.cumulus.base.BaseMapper;
import com.cumulus.modules.business.gather.dto.GatherRecordDto;
import com.cumulus.modules.business.gather.entity.mysql.GatherRecord;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 采集记录传输对象与采集记录实体的映射
 *
 * @author zhangxq
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GatherRecordMapper extends BaseMapper<GatherRecordDto, GatherRecord> {
}
