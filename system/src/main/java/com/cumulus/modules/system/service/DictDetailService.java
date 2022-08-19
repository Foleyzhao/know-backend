package com.cumulus.modules.system.service;

import com.cumulus.modules.system.entity.DictDetail;
import com.cumulus.modules.system.dto.DictDetailDto;
import com.cumulus.modules.system.dto.DictDetailQueryCriteria;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 系统字典详情服务接口
 */
public interface DictDetailService {

    /**
     * 创建字典详情
     *
     * @param resources 字典详情
     */
    void create(DictDetail resources);

    /**
     * 编辑字典详情
     *
     * @param resources 字典详情
     */
    void update(DictDetail resources);

    /**
     * 根据字典详情ID删除字典详情
     *
     * @param id 字典详情ID
     */
    void delete(Long id);

    /**
     * 分页查询
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return 字典列表
     */
    Map<String, Object> queryAll(DictDetailQueryCriteria criteria, Pageable pageable);

    /**
     * 根据字典名称获取字典详情列表
     *
     * @param name 字典名称
     * @return 字典详情列表
     */
    List<DictDetailDto> getDictByName(String name);

}
