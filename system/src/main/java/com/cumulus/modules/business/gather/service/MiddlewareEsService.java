package com.cumulus.modules.business.gather.service;

import org.springframework.data.domain.Pageable;

/**
 * 中间件接口服务
 *
 * @author Shijh
 */
public interface MiddlewareEsService {

    /**
     *
     * @param id
     * @param pageable
     * @return
     */
    Object findListRecent(String id, Pageable pageable);

}
