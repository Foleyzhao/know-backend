package com.cumulus.modules.business.gather.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.entity.es.SystemProcessEs;
import com.cumulus.modules.business.gather.repository.SystemProcessEsRepository;
import com.cumulus.modules.business.gather.service.SystemProcessesEsService;
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
 * 系统进程接口服务实现
 *
 * @author Shijh
 */
@Slf4j
@Service
public class SystemProcessesEsServiceImpl implements SystemProcessesEsService {

    /**
     * 服务进程数据访问接口
     */
    @Autowired
    private SystemProcessEsRepository systemProcessesRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAll(List<SystemProcessEs> systemProcesses) {
        this.systemProcessesRepository.saveAll(systemProcesses);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(String id) {
        this.systemProcessesRepository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateById(SystemProcessEs systemProcesses) {
        this.systemProcessesRepository.save(systemProcesses);
    }

    @Override
    public Object findListRecent(String id, Pageable pageable) {
        //找到utime 最新的记录
        PageRequest uTime = PageRequest.of(0, 1, Sort.Direction.DESC, "utime");
        Page<SystemProcessEs> topOne = systemProcessesRepository.findAllByGatherAssetId(id, uTime);
        if (topOne.getContent().isEmpty()) {
            return PageUtils.toPage(new PageImpl<GatherAssetEs>(new ArrayList<>()));
        }
        //找到时间和最新的utime相等的数据
        Date topTime = topOne.getContent().get(0).getUtime();
        return PageUtils.toPage(systemProcessesRepository.findAllByGatherAssetIdAndUtime(id, topTime, pageable));
    }
}
