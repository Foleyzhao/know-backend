package com.cumulus.modules.business.gather.service.impl;

import java.util.ArrayList;
import java.util.Date;
import com.cumulus.modules.business.gather.entity.es.AccountEs;
import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.entity.es.MiddlewareEs;
import com.cumulus.modules.business.gather.repository.MiddlewareEsRepository;
import com.cumulus.modules.business.gather.service.MiddlewareEsService;
import com.cumulus.utils.PageUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * 中间件-接口实现
 *
 * @author Shijh
 */
@Slf4j
@Service
public class MiddlewareEsServiceImpl implements MiddlewareEsService {

    @Autowired
    private MiddlewareEsRepository middlewareEsRepository;

    @Override
    public Object findListRecent(String id, Pageable pageable) {
        //找到utime 最新的记录
        PageRequest uTime = PageRequest.of(0, 1, Sort.Direction.DESC, "utime");
        Page<MiddlewareEs> topOne = middlewareEsRepository.findAllByGatherAssetId(id, uTime);
        if (topOne.getContent().isEmpty()) {
            return PageUtils.toPage(new PageImpl<GatherAssetEs>(new ArrayList<>()));
        }
        //找到时间和最新的utime相等的数据
        Date topTime = topOne.getContent().get(0).getUtime();
        return PageUtils.toPage(middlewareEsRepository.findAllByGatherAssetIdAndUtime(id, topTime, pageable));
    }
}
