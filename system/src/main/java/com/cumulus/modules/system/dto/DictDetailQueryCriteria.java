package com.cumulus.modules.system.dto;

import com.cumulus.annotation.Query;
import lombok.Data;

/**
 * 系统字典详情查询传输对象
 */
@Data
public class DictDetailQueryCriteria {

    /**
     * 字典详情标签
     */
    @Query(type = Query.Type.INNER_LIKE)
    private String label;

    /**
     * 字典名称
     */
    @Query(propName = "name", joinName = "dict")
    private String dictName;

}