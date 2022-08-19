package com.cumulus.modules.business.gather.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.entity.es.HistoryEs;
import com.cumulus.modules.business.gather.repository.AssetEsRepository;
import com.cumulus.modules.business.gather.repository.HistoryEsRepository;
import com.cumulus.modules.business.gather.service.AssetEsService;
import com.cumulus.modules.business.gather.service.HistoryEsService;
import com.cumulus.utils.PageUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 历史变更接口实现
 *
 * @author Shijh
 */
@Slf4j
@Service
public class HistoryEsServiceImpl implements HistoryEsService {

    /**
     * es模板
     */
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    /**
     * 历史记录数据访问接口
     */
    @Autowired
    private HistoryEsRepository historyEsRepository;

    /**
     * 资产数据访问接口
     */
    @Autowired
    private AssetEsRepository assetRepository;

    /**
     * 资产接口服务
     */
    @Autowired
    private AssetEsService assetService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAll(List<HistoryEs> historyEsList) {
        this.historyEsRepository.saveAll(historyEsList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(String id) {
        this.historyEsRepository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateById(HistoryEs historyEs) {
        this.historyEsRepository.save(historyEs);
    }

    @Override
    public Object findListRecent(Long id, Pageable pageable) {
        if (null == id) {
            return new PageImpl<GatherAssetEs>(new ArrayList<>());
        }
        return PageUtils.toPage(historyEsRepository.findAllByAssetIdAndSource(id, pageable,"update"));
        //todo shenjc 历史记录 还没有设计
//        Optional<GatherAssetEs> assetOptional = assetRepository.findById(id);
//        if (assetOptional.isPresent()) {
//            return PageUtils.toPage(historyEsRepository.findAllById(assetOptional.get().getId(), pageable));
//        }
//        return new PageImpl<GatherAssetEs>(new ArrayList<>());
    }

}
