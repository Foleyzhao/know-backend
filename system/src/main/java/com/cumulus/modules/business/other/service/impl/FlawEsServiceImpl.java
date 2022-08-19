package com.cumulus.modules.business.other.service.impl;


import java.util.List;
import java.util.stream.Collectors;
import com.cumulus.modules.business.other.entity.es.FlawEs;
import com.cumulus.modules.business.other.repository.FlawEsRepository;
import com.cumulus.modules.business.other.service.FlawEsService;
import com.cumulus.utils.PageUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 漏洞接口实现
 *
 * @author Shijh
 */
@Slf4j
@Service
public class FlawEsServiceImpl implements FlawEsService {

    /**
     * 漏洞数据访问接口
     */
    @Autowired
    private FlawEsRepository flawEsRepository;

    /**
     * es模板
     */
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAll(List<FlawEs> flawEs) {
        this.flawEsRepository.saveAll(flawEs);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(String id) {
        this.flawEsRepository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateById(FlawEs flawEs) {
        this.flawEsRepository.save(flawEs);
    }

    @Override
    public Object finAll(Pageable pageable) {
        return this.flawEsRepository.findAll(pageable);
    }

    @Override
    public Object getFlawInformation(String ip,Pageable pageable) {
        CriteriaQuery criteriaQuery = new CriteriaQuery(new Criteria()
                .and(new Criteria("ip").is(ip)))
                .addSort(Sort.by(Sort.Order.desc("ut`ime"))).setPageable(pageable);
        SearchHits<FlawEs> safetyCount = this.elasticsearchRestTemplate.search(criteriaQuery, FlawEs.class);
        List<FlawEs> collect = safetyCount.get().map(SearchHit::getContent).collect(Collectors.toList());
        return PageUtils.toPage(collect, safetyCount.getTotalHits());
    }

}
