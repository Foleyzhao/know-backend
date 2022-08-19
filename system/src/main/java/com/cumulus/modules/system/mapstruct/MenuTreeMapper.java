package com.cumulus.modules.system.mapstruct;

import com.cumulus.base.BaseMapper;
import com.cumulus.modules.system.dto.MenuTreeDto;
import com.cumulus.modules.system.entity.Menu;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 菜单树结构Dto Mapper
 *
 * @author : shenjc
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MenuTreeMapper extends BaseMapper<MenuTreeDto, Menu> {
}
