package com.cumulus.modules.business.gather.service.impl;

import java.util.ArrayList;
import java.util.Date;
import com.cumulus.modules.business.gather.entity.es.DBEs;
import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.entity.es.PortEs;
import com.cumulus.modules.business.gather.repository.DBEsRepository;
import com.cumulus.modules.business.gather.service.DBEsService;
import com.cumulus.utils.PageUtils;

import liquibase.pro.packaged.D;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * 数据库-接口实现
 *
 * @author Shijh
 */
@Slf4j
@Service
public class DBEsServiceImpl implements DBEsService {

    /**
     *  数据库数据访问接口
     */
    @Autowired
    private DBEsRepository dbEsRepository;

    @Override
    public Object findListRecent(String id, Pageable pageable) {
        //找到utime 最新的记录
        PageRequest uTime = PageRequest.of(0, 1, Sort.Direction.DESC, "utime");
        Page<DBEs> topOne = dbEsRepository.findAllByGatherAssetId(id, uTime);
        if (topOne.getContent().isEmpty()) {
            return PageUtils.toPage(new PageImpl<GatherAssetEs>(new ArrayList<>()));
        }
        //找到时间和最新的utime相等的数据
        Date topTime = topOne.getContent().get(0).getUtime();
        return PageUtils.toPage(dbEsRepository.findAllByGatherAssetIdAndUtime(id, topTime, pageable));
    }
}
