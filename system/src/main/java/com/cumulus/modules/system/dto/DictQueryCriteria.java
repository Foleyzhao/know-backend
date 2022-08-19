package com.cumulus.modules.system.dto;

import com.cumulus.annotation.Query;
import lombok.Data;

/**
 * 系统字典查询传输对象
 */
@Data
public class DictQueryCriteria {

    /**
     * 模糊查询内容
     */
    @Query(blurry = "name,description")
    private String blurry;

}
