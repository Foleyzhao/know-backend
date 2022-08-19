package com.cumulus.modules.business.gather.service.impl;

import java.util.ArrayList;
import java.util.Date;
import com.cumulus.modules.business.gather.entity.es.AccountEs;
import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.entity.es.ScanBasicInfoEs;
import com.cumulus.modules.business.gather.repository.ScanBasicInfoEsRepository;
import com.cumulus.modules.business.gather.service.ScanBasicInfoEsService;
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
 * 远程扫描-基本信息服务接口实现
 *
 * @author Shijh
 */
@Service
@Slf4j
public class ScanBasicInfoEsServiceImpl implements ScanBasicInfoEsService {

    /**
     * 远程扫描-数据访问接口
     */
    @Autowired
    private ScanBasicInfoEsRepository scanBasicInfoEsRepository;

    @Override
    public Object findListRecent(String id, Pageable pageable) {
        //找到utime 最新的记录
        PageRequest uTime = PageRequest.of(0, 1, Sort.Direction.DESC, "utime");
        Page<ScanBasicInfoEs> topOne = scanBasicInfoEsRepository.findAllByScanAssetId(id, uTime);
        if (topOne.getContent().isEmpty()) {
            return PageUtils.toPage(new PageImpl<ScanBasicInfoEs>(new ArrayList<>()));
        }
        //找到时间和最新的utime相等的数据
        Date topTime = topOne.getContent().get(0).getUtime();
        return PageUtils.toPage(scanBasicInfoEsRepository.findAllByScanAssetIdAndUtime(id, topTime, pageable));
    }
}
