package com.cumulus.modules.business.gather.service.impl;

import com.cumulus.modules.business.gather.entity.es.BasicInfoEs;
import com.cumulus.modules.business.gather.repository.BasicInfoEsRepository;
import com.cumulus.modules.business.gather.service.BasicInfoEsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;

/**
 * 基本信息Es接口实现类
 *
 * @author : shenjc
 */
@Service
public class BasicInfoEsServiceImpl implements BasicInfoEsService {
    @Autowired
    private BasicInfoEsRepository basicInfoEsRepository;

    @Override
    public Page<BasicInfoEs> findListRecent(String id, Pageable pageable) {
        //找到utime 最新的记录
        PageRequest uTime = PageRequest.of(0, 1, Sort.Direction.DESC, "utime");
        Page<BasicInfoEs> topOne = basicInfoEsRepository.findAllByGatherAssetIdEquals(id, uTime);
        if (topOne.getContent().isEmpty()) {
            return new PageImpl<>(new ArrayList<>());
        }
        //找到时间和最新的utime相等的数据
        Date topTime = topOne.getContent().get(0).getUtime();
        return basicInfoEsRepository.findAllByGatherAssetIdAndUtime(id, topTime, pageable);
    }
}


