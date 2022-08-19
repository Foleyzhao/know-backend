package com.cumulus.modules.business.gather.service.impl;

import com.cumulus.modules.business.gather.common.constant.GatherConstants;
import com.cumulus.modules.business.gather.common.utils.CommUtils;
import com.cumulus.modules.business.gather.entity.es.GatherAssetLogEs;
import com.cumulus.modules.business.gather.repository.GatherAssetLogEsRepository;
import com.cumulus.modules.business.gather.service.GatherAssetLogEsService;
import com.cumulus.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 资产采集日志服务实现
 *
 * @author zhaoff
 */
@Slf4j
@Service
public class GatherAssetLogEsServiceImpl implements GatherAssetLogEsService {

    /**
     * ES模板
     */
    @Autowired
    private ElasticsearchRestTemplate esTemplate;

    /**
     * 资产采集日志数据访问接口
     */
    @Autowired
    private GatherAssetLogEsRepository gatherAssetLogEsRepository;

    @SuppressWarnings("unchecked")
    @Override
    public Map<Integer, Long> taskResultStatistics(String taskLogId) {
        Map<Integer, Long> statResult = new HashMap<>();
        if (StringUtils.isEmpty(taskLogId)) {
            return statResult;
        }
        try {
            // 根据result字段进行桶分组
            TermsAggregationBuilder resultAgg = AggregationBuilders.terms("resultAgg").field("result");
            // 构建查询语句
            Query query = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("taskLogId", taskLogId)))
                    .withAggregations(resultAgg).build();
            // 执行查询
            SearchHits<GatherAssetLogEs> gatherAssetLogHits = esTemplate.search(query, GatherAssetLogEs.class);
            // 解析数据
            Aggregations aggregations = (Aggregations) gatherAssetLogHits.getAggregations();
            if (null != aggregations) {
                Terms agg = aggregations.get("resultAgg");

                List<Terms.Bucket> buckets = (List<Terms.Bucket>) agg.getBuckets();
                for (Terms.Bucket bucket : buckets) {
                    statResult.put(Integer.parseInt(bucket.getKeyAsString()), bucket.getDocCount());
                }
            }
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn(String.format("Query result of gather task  exception, taskLogId=%s.", taskLogId), e);
            }
        }
        return statResult;
    }

    @Override
    public List<GatherAssetLogEs> getUnGatherTask(Collection<Long> planIds, int size) {
        List<GatherAssetLogEs> result = new ArrayList<>();
        if (CommUtils.isEmptyOfCollection(planIds)) {
            return null;
        }
        if (size < 1) {
            size = 100;
        }
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQuery = new BoolQueryBuilder();
        boolQuery.must(QueryBuilders.termsQuery("planId", planIds));
        BoolQueryBuilder shouldQuery = new BoolQueryBuilder();
        shouldQuery.should(QueryBuilders.matchQuery("state", 0));
        boolQuery.must(shouldQuery);
        Query query = queryBuilder.withQuery(boolQuery)
                .withSorts(SortBuilders.fieldSort("create").order(SortOrder.ASC))
                .withPageable(PageRequest.of(0, size)).build();
        SearchHits<GatherAssetLogEs> hits = esTemplate.search(query, GatherAssetLogEs.class);
        hits.forEach(hit -> {
            result.add(hit.getContent());
        });
        return result;
    }

    @Override
    public List<String> getRunningTasksGatherId(Long planId, Boolean userAgent) {
        List<Integer> states = Collections.singletonList(GatherConstants.STATE_RUNNING);
        try {
            // TODO zhaoff 改造滚动查询改造
            List<String> gatherIds = new ArrayList<>();
            if (null == planId) {
                return gatherIds;
            }
            int pageSize = 500;
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery().must(QueryBuilders.termQuery("planId", planId));
            if (null != userAgent) {
                boolQuery.must(QueryBuilders.matchQuery("useAgent", userAgent));
            }
            if (!CommUtils.isEmptyOfCollection(states)) {
                boolQuery.must(QueryBuilders.termsQuery("state", states));
            }
            boolQuery.mustNot(QueryBuilders.existsQuery("end"));
            NativeSearchQueryBuilder queryBuilder =
                    new NativeSearchQueryBuilder().withPageable(PageRequest.of(0, pageSize));
            Query searchQuery = queryBuilder.withFilter(boolQuery).withFields("gatherId").build();

            SearchHits<GatherAssetLogEs> hits = esTemplate.search(searchQuery, GatherAssetLogEs.class);
            hits.forEach(hit -> {
                gatherIds.add(hit.getContent().getGatherId());
            });
            return gatherIds;
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn(String.format("Query all gathering tasks for plan:%s.", planId), e);
            }
        }
        return new ArrayList<>();
    }

    @Override
    public List<String> getUnCompletedGatherId(Long planId, Boolean userAgent) {
        List<String> gatherIds = new ArrayList<>();
        try {
            // TODO zhaoff 改造滚动查询
            int pageSize = 500;
            BoolQueryBuilder boolQuery =
                    QueryBuilders.boolQuery().must(QueryBuilders.termQuery("planId", planId));
            if (null != userAgent) {
                boolQuery.must(QueryBuilders.matchQuery("useAgent", userAgent));
            }
            boolQuery.mustNot(QueryBuilders.existsQuery("end"));
            NativeSearchQueryBuilder queryBuilder =
                    new NativeSearchQueryBuilder().withPageable(PageRequest.of(0, pageSize));
            Query searchQuery = queryBuilder.withFilter(boolQuery).withFields("gatherId").build();
            SearchHits<GatherAssetLogEs> hits = esTemplate.search(searchQuery, GatherAssetLogEs.class);
            hits.forEach(hit -> {
                gatherIds.add(hit.getContent().getGatherId());
            });
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn(String.format("Query all unStart tasks for plan:%s.", planId), e);
            }
        }
        return gatherIds;
    }

    @Override
    public void saveAll(List<GatherAssetLogEs> gatherAssetLogs) {
        gatherAssetLogEsRepository.saveAll(gatherAssetLogs);
    }

}
