package com.cumulus.modules.system.mapstruct;

import com.cumulus.base.BaseMapper;
import com.cumulus.modules.system.dto.SimpMessageDTO;
import com.cumulus.modules.system.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * 系统消息表DTO 和实体的mapper 会使用到 DICT表的查询方法 所以是 抽象类用于注入 字典表的服务
 *
 * @author : shenjc
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SimpMessageMapper extends BaseMapper<SimpMessageDTO, Message> {

    /**
     * 实体转DTO
     *
     * @param entity 实体
     * @return 返回DTO
     */
    @Override
    @Mapping(target = "messageTypeStr", expression = "java(com.cumulus.enums.MessageTypeEnum.getNameByType(entity.getMessageType()))")
    SimpMessageDTO toDto(Message entity);
}
