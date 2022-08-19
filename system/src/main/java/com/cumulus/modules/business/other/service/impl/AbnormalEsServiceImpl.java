package com.cumulus.modules.business.other.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import com.cumulus.modules.business.other.entity.es.AbnormalEs;
import com.cumulus.modules.business.other.repository.AbnormalEsRepository;
import com.cumulus.modules.business.other.service.AbnormalEsService;
import com.cumulus.utils.PageUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
 * 异常接口实现类
 *
 * @author Shijh
 */
@Slf4j
@Service
public class AbnormalEsServiceImpl implements AbnormalEsService{

    /**
     * es模板
     */
    @Lazy
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    /**
     * 异常数据访问接口
     */
    @Autowired
    private AbnormalEsRepository abnormalEsRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAll(List<AbnormalEs> abnormalEs) {
        this.abnormalEsRepository.saveAll(abnormalEs);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(String id) {
        this.abnormalEsRepository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateById(AbnormalEs abnormalEs) {
        this.abnormalEsRepository.save(abnormalEs);
    }

    @Override
    public Object findList(String id, Pageable pageable) {
        return null;
    }

    @Override
    public Object getAbnormal(String ip, Pageable pageable) {

        CriteriaQuery criteriaQuery = new CriteriaQuery(new Criteria()
                .and(new Criteria("ip").is(ip)))
                .addSort(Sort.by(Sort.Order.desc("utime"))).setPageable(pageable);
        SearchHits<AbnormalEs> safetyCount = this.elasticsearchRestTemplate.search(criteriaQuery, AbnormalEs.class);
        List<AbnormalEs> collect = safetyCount.get().map(SearchHit::getContent).collect(Collectors.toList());
        return PageUtils.toPage(collect, safetyCount.getTotalHits());
    }

}
