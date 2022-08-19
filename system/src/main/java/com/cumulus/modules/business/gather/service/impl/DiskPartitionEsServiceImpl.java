package com.cumulus.modules.business.gather.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.cumulus.modules.business.gather.entity.es.DiskPartitionEs;
import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.repository.DiskPartitionEsRepository;
import com.cumulus.modules.business.gather.service.DiskPartitionEsService;
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
 * 磁盘信息服务接口的实现
 *
 * @author shijh
 */
@Slf4j
@Service
public class DiskPartitionEsServiceImpl implements DiskPartitionEsService {

    /**
     * 磁盘信息数据访问接口
     */
    @Autowired
    private DiskPartitionEsRepository diskPartitionEsRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAll(List<DiskPartitionEs> diskPartitionEs) {
        this.diskPartitionEsRepository.saveAll(diskPartitionEs);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(String id) {
        this.diskPartitionEsRepository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateById(DiskPartitionEs diskPartitionEs) {
        this.diskPartitionEsRepository.save(diskPartitionEs);
    }

    @Override
    public Object findListRecent(String id, Pageable pageable) {
        //找到utime 最新的记录
        PageRequest uTime = PageRequest.of(0, 1, Sort.Direction.DESC, "utime");
        Page<DiskPartitionEs> topOne = diskPartitionEsRepository.findAllByGatherAssetId(id, uTime);
        if (topOne.getContent().isEmpty()) {
            return PageUtils.toPage(new PageImpl<GatherAssetEs>(new ArrayList<>()));
        }
        //找到时间和最新的utime相等的数据
        Date topTime = topOne.getContent().get(0).getUtime();
        return PageUtils.toPage(diskPartitionEsRepository.findAllByGatherAssetIdAndUtime(id, topTime, pageable));
    }
}
