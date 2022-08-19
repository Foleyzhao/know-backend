package com.cumulus.modules.business.gather.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.entity.es.NetworkEs;
import com.cumulus.modules.business.gather.repository.NetworkEsRepository;
import com.cumulus.modules.business.gather.service.NetworkEsService;

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
 * 网络配置接口服务实现
 *
 * @author Shijh
 */
@Slf4j
@Service
public class NetworkEsServiceImpl implements NetworkEsService {

    /**
     * 网络配置数据访问接口
     */
    @Autowired
    private NetworkEsRepository networkEsRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAll(List<NetworkEs> networkEsList) {
        this.networkEsRepository.saveAll(networkEsList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(String id) {
        this.networkEsRepository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateById(NetworkEs networkEs) {
        this.networkEsRepository.save(networkEs);
    }

    @Override
    public Object findListRecent(String id, Pageable pageable) {
        //找到utime 最新的记录
        PageRequest uTime = PageRequest.of(0, 1, Sort.Direction.DESC, "utime");
        Page<NetworkEs> topOne = networkEsRepository.findAllByGatherAssetId(id, uTime);
        if (topOne.getContent().isEmpty()) {
            return PageUtils.toPage(new PageImpl<GatherAssetEs>(new ArrayList<>()));
        }
        //找到时间和最新的utime相等的数据
        Date topTime = topOne.getContent().get(0).getUtime();
        return PageUtils.toPage(networkEsRepository.findAllByGatherAssetIdAndUtime(id, topTime, pageable));
    }
}
