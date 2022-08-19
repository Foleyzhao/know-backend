package com.cumulus.modules.business.gather.service;

import com.cumulus.modules.business.gather.entity.es.BasicInfoEs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 基本信息服务接口
 *
 * @author : shenjc
 */
public interface BasicInfoEsService {

    /**
     * 分页查询
     *
     * @param id       资产表的id
     * @param pageable 分页条件
     * @return 分页结果
     */
    Page<BasicInfoEs> findListRecent(String id, Pageable pageable);
}
