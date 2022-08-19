package com.cumulus.modules.business.gather.service;

import org.springframework.data.domain.Pageable;

/**
 * 数据库接口服务
 *
 * @author Shijh
 */
public interface DBEsService {

    Object findListRecent(String id, Pageable pageable);
}
