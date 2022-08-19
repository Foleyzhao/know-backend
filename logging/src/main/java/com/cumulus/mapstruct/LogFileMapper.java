package com.cumulus.mapstruct;

import com.cumulus.base.BaseMapper;
import com.cumulus.dto.LogFileDTO;
import com.cumulus.entity.LogFile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * 日志归档表 mapper
 *
 * @author : shenjc
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LogFileMapper extends BaseMapper<LogFileDTO, LogFile> {

    /**
     * 使用静态方法将 归档类型 由 数字 转中文;
     *
     * @param entity 实体
     * @return DTO对象
     */
    @Override
    @Mapping(target = "fileType", expression = "java(com.cumulus.dto.LogFileDTO.getFileTypeStr(entity.getFileType()))")
    LogFileDTO toDto(LogFile entity);
}
