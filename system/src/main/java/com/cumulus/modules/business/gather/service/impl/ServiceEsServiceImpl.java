package com.cumulus.modules.business.gather.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.entity.es.ServiceEs;
import com.cumulus.modules.business.gather.repository.ServiceEsRepository;
import com.cumulus.modules.business.gather.service.ServiceEsService;
import com.cumulus.utils.PageUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务-接口实现
 *
 * @author Shijh
 */
@Slf4j
@Service
public class ServiceEsServiceImpl implements ServiceEsService {

    /**
     * 服务数据访问接口
     */
    @Autowired
    private ServiceEsRepository serveRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAll(List<ServiceEs> serviceEsList) {
        this.serveRepository.saveAll(serviceEsList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(String id) {
        this.serveRepository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateById(ServiceEs serviceEs) {
        this.serveRepository.save(serviceEs);
    }

    @Override
    public Object findListRecent(String id, Pageable pageable) {
        //找到utime 最新的记录
        PageRequest uTime = PageRequest.of(0, 1, Sort.Direction.DESC, "utime");
        Page<ServiceEs> topOne = serveRepository.findAllByGatherAssetId(id, uTime);
        if (topOne.getContent().isEmpty()) {
            return PageUtils.toPage(new PageImpl<GatherAssetEs>(new ArrayList<>()));
        }
        //找到时间和最新的utime相等的数据
        Date topTime = topOne.getContent().get(0).getUtime();
        return PageUtils.toPage(serveRepository.findAllByGatherAssetIdAndUtime(id, topTime, pageable));
    }

}
