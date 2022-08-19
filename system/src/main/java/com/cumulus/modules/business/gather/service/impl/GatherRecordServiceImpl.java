package com.cumulus.modules.business.gather.service.impl;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.cumulus.modules.business.gather.entity.es.GatherResultEs;
import com.cumulus.modules.business.gather.repository.GatherPeriodRepository;
import com.cumulus.modules.business.gather.repository.GatherPlanRepository;
import com.cumulus.modules.business.gather.service.GatherRecordService;
import com.cumulus.modules.business.gather.vo.GatherRecordVo;
import com.cumulus.utils.DateUtils;
import com.cumulus.utils.PageUtils;

import lombok.SneakyThrows;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

/**
 * 采集记录服务实现类
 *
 * @author zhangxq
 */
@Service
public class GatherRecordServiceImpl implements GatherRecordService {

    /**
     * ES模板
     */
    @Autowired
    private ElasticsearchRestTemplate esTemplate;

    @Autowired
    private GatherPlanRepository gatherPlanRepository;


    @SneakyThrows
    @SuppressWarnings(value = "unchecked")
    @Override
    public Object queryAll(GatherRecordVo gatherRecordVo, Pageable pageable) {
        List<HashMap<String, Object>> res = new ArrayList<>();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        if (null != gatherRecordVo.getBlurry()) {
            BoolQueryBuilder ip = QueryBuilders.boolQuery().must(QueryBuilders.wildcardQuery("planName", "*" + gatherRecordVo.getBlurry() + "*"));
            BoolQueryBuilder name = QueryBuilders.boolQuery().must(QueryBuilders.wildcardQuery("assetIp", "*" + gatherRecordVo.getBlurry() + "*"));
            boolQueryBuilder = QueryBuilders.boolQuery().filter(boolQueryBuilder.should(name).should(ip));
        }
        // ip
        if (null != gatherRecordVo.getAssetIp()) {
            boolQueryBuilder.must(QueryBuilders.wildcardQuery("assetIp", "*" + gatherRecordVo.getAssetIp() + "*"));
        }
        // 名称
        if (null != gatherRecordVo.getName()) {
            boolQueryBuilder.must(QueryBuilders.wildcardQuery("planName", "*" + gatherRecordVo.getName() + "*"));
        }
        // 采集结果
        if (null != gatherRecordVo.getResult() && !gatherRecordVo.getResult().contains(4)) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("result", gatherRecordVo.getResult()));
        }
        //近期更新
        if (null != gatherRecordVo.getStartTime() && null != gatherRecordVo.getEndTime()) {
            SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.YYYY_MM_DD_HH_MM_SS_STR);
            Date startTime = sdf.parse(gatherRecordVo.getStartTime());
            Date endTime = sdf.parse(gatherRecordVo.getEndTime());
            RangeQueryBuilder updateDate = QueryBuilders.rangeQuery("begin")
                    .gte(startTime)
                    .lte(endTime);
            boolQueryBuilder.must(updateDate);
        }
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder().withPageable(pageable);
        // 排序字段
        if (null != gatherRecordVo.getField()) {
            queryBuilder.withSorts(SortBuilders.fieldSort(gatherRecordVo.getField()).order(SortOrder.fromString(gatherRecordVo.getOrder())));
        }
        Query query = queryBuilder.withQuery(boolQueryBuilder).build();
        SearchHits<GatherResultEs> search = esTemplate.search(query, GatherResultEs.class);
        long count = esTemplate.count(query, GatherResultEs.class);
        search.forEach(hit -> {
            HashMap<String, Object> content = new HashMap<>();
            content.put("planName", hit.getContent().getPlanName());
            content.put("begin", hit.getContent().getBegin());
            content.put("gatherObj", gatherPlanRepository.findById(hit.getContent().getPlanId()).orElse(null).getGatherNum());
            content.put("frequently", hit.getContent().getFrequently());
            content.put("stationary", hit.getContent().getStationary());
            content.put("seldom", hit.getContent().getSeldom());
            content.put("planId", hit.getContent().getPlanId());
            content.put("end", hit.getContent().getEnd());
            res.add(content);
        });
        return PageUtils.toPage(res, count);
    }

    @SneakyThrows
    @Override
    public Object countRecord() {
        Map<String, Object> resultMap = new HashMap<>();
        List<Integer> list = Arrays.asList(0, 1, 2, 3);
        for (Integer res : list) {
            long count = getCount(res);
            if (res == 0) resultMap.put("successCount", count);
            if (res == 1) resultMap.put("fail", count);
            if (res == 2) resultMap.put("partialSuccess", count);
            if (res == 3) resultMap.put("all", count);
        }
        return resultMap;
    }

    /**
     * 统计
     *
     * @param result 条件
     * @return 结果
     */
    private long getCount(Integer result) {
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        if (result != 3) {
            boolQueryBuilder.must(QueryBuilders.termQuery("result", result));
        }
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        Query query = queryBuilder.withQuery(boolQueryBuilder).build();
        return esTemplate.count(query, GatherResultEs.class);
    }

}
