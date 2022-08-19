package com.cumulus.modules.system.mapstruct;

import com.cumulus.base.BaseMapper;
import com.cumulus.modules.system.entity.Dept;
import com.cumulus.modules.system.dto.DeptDto;
import com.cumulus.modules.system.service.UserService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 系统部门传输对象与系统部门实体的映射
 *
 * @author shenjc
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class DeptMapper implements BaseMapper<DeptDto, Dept> {

    @Autowired
    UserService userService;

    /**
     * 将实体转换为DTO 使用 getDeptHead() 方法获取部门负责人
     *
     * @param entity 实体类
     * @return 返回DTO
     */
    @Override
    @Mapping(target = "deptHead", expression = "java(userService.getDeptHead(entity.getId()))")
    public abstract DeptDto toDto(Dept entity);
}