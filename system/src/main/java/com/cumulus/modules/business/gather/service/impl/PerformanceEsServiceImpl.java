package com.cumulus.modules.business.gather.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.entity.es.PerformanceEs;
import com.cumulus.modules.business.gather.repository.PerformanceEsRepository;
import com.cumulus.modules.business.gather.service.PerformanceEsService;
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
 * 性能监控接口实现
 *
 * @author Shijh
 */
@Slf4j
@Service
public class PerformanceEsServiceImpl implements PerformanceEsService {

    /**
     * 性能信息数据访问接口
     */
    @Autowired
    private PerformanceEsRepository performanceEsRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAll(List<PerformanceEs> performanceEsList) {
        this.performanceEsRepository.saveAll(performanceEsList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(String id) {
        this.performanceEsRepository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateById(PerformanceEs performanceEs) {
        this.performanceEsRepository.save(performanceEs);
    }

    @Override
    public Object findList(String id, Pageable pageable) {
        return PageUtils.toPage(performanceEsRepository.findAllByGatherAssetId(id, pageable));
    }

}
