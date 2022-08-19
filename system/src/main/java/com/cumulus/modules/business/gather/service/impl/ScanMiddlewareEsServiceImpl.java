package com.cumulus.modules.business.gather.service.impl;

import java.util.ArrayList;
import java.util.Date;
import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.entity.es.ScanMiddlewareEs;
import com.cumulus.modules.business.gather.repository.ScanMiddlewareEsRepository;
import com.cumulus.modules.business.gather.service.ScanMiddlewareEsService;
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
 * 远程扫描-中间件-接口实现
 *
 * @author Shijh
 */
@Slf4j
@Service
public class ScanMiddlewareEsServiceImpl implements ScanMiddlewareEsService {

    /**
     * 远程扫描-中间件数据访问接口
     */
    @Autowired
    private ScanMiddlewareEsRepository scanMiddlewareEsRepository;

    @Override
    public Object findListRecent(String id, Pageable pageable) {
        //找到utime 最新的记录
        PageRequest uTime = PageRequest.of(0, 1, Sort.Direction.DESC, "utime");
        Page<ScanMiddlewareEs> topOne = scanMiddlewareEsRepository.findAllByScanAssetId(id, uTime);
        if (topOne.getContent().isEmpty()) {
            return PageUtils.toPage(new PageImpl<GatherAssetEs>(new ArrayList<>()));
        }
        //找到时间和最新的utime相等的数据
        Date topTime = topOne.getContent().get(0).getUtime();
        return PageUtils.toPage(scanMiddlewareEsRepository.findAllByScanAssetIdAndUtime(id, topTime, pageable));
    }
}
