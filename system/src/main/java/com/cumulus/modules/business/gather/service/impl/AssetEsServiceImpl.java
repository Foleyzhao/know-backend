package com.cumulus.modules.business.gather.service.impl;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import com.alibaba.fastjson.JSON;
import com.cumulus.constant.FileConstant;
import com.cumulus.enums.DetailedFileTypeEnum;
import com.cumulus.modules.business.entity.Asset;
import com.cumulus.modules.business.entity.AssetTag;
import com.cumulus.modules.business.gather.entity.es.AssetEs;
import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.repository.AssetEsRepository;
import com.cumulus.modules.business.gather.service.AssetEsService;
import com.cumulus.modules.business.gather.vo.AssetsWarehouseVo;
import com.cumulus.modules.business.repository.AssetRepository;
import com.cumulus.modules.system.service.DetailedFileService;
import com.cumulus.utils.DateUtils;
import com.cumulus.utils.FileUtils;
import com.cumulus.utils.PageUtils;
import cn.hutool.core.io.IoUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ????????????????????????
 *
 * @author Shijh
 */
@Slf4j
@Service
public class AssetEsServiceImpl implements AssetEsService {

    /**
     * es??????
     */
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    /**
     * ????????????????????????es
     */
    @Autowired
    private AssetEsRepository assetRepository;

    /**
     * ????????????????????????
     */
    @Autowired
    private AssetRepository assetRep;

    /**
     * ????????????????????????
     */
    @Autowired
    private DetailedFileService detailedFileService;

    /**
     * ????????????????????????
     */
    @Autowired
    private RestHighLevelClient client;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAll(List<GatherAssetEs> assets) {
        HashSet<Object> set = new HashSet<>();
        Iterable<GatherAssetEs> all = this.assetRepository.findAll();
        ok:
        for (GatherAssetEs assetEs : all) {
            String ip = assetEs.getIp();
            for (GatherAssetEs asset : assets) {
                if (asset.getIp().equals(ip)) {
                    assetEs.setHasPort(1);
                    this.assetRepository.save(assetEs);
                    set.add(assetEs.getIp());
                    continue ok;
                }
            }

        }
        for (GatherAssetEs asset : assets) {
            if (set.contains(asset.getIp())) {
                asset.setHasPort(1);
            }
            set.add(asset.getIp());
        }
        this.assetRepository.saveAll(assets);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(String id) {
        this.assetRepository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateById(GatherAssetEs asset) {
        this.assetRepository.save(asset);
    }

    @Override
    public GatherAssetEs findById(String id) {
        return this.assetRepository.findById(id).orElse(null);
    }

    @SneakyThrows
    @Override
    public Object findByPortAsset(String ip, Pageable pageable, AssetsWarehouseVo assetsWarehouseVo) {
        SearchRequest searchRequest = new SearchRequest("asset");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.termQuery("ip", ip))
                .must(QueryBuilders.termQuery("diff", 1));
        findConditions(assetsWarehouseVo, sourceBuilder, queryBuilder);
        sourceBuilder.query(queryBuilder);
        sourceBuilder.size(pageable.getPageSize());
        sourceBuilder.from((int) pageable.getOffset());
        searchRequest.source(sourceBuilder);
        SearchResponse search = this.client.search(searchRequest, RequestOptions.DEFAULT);
        List<AssetEs> lists = new ArrayList<>();
        SearchHit[] hits = search.getHits().getHits();
        for (SearchHit hit : hits) {
            String sourceAsString = hit.getSourceAsString();
            AssetEs assetEs = JSON.parseObject(sourceAsString, AssetEs.class);
            lists.add(assetEs);
        }
        return PageUtils.toPage(lists, lists.size());
    }

    @Override
    public Object countAssetStart() {
        AssetsWarehouseVo assetsWarehouseVo = new AssetsWarehouseVo();
        List<Integer> list = Arrays.asList(0, 1, 2, 3);
        for (Integer start : list) {
            if (AssetEs.SURVIVE == start) {
                assetsWarehouseVo.setSurvive(searchStartAsset(start));
            } else if (AssetEs.ABNORMAL == start) {
                assetsWarehouseVo.setAbnormal(searchStartAsset(start));
            } else if (AssetEs.DOWN_LINE == start) {
                assetsWarehouseVo.setDownLine(searchStartAsset(start));
            }
        }
        for (Integer risk : list) {
            if (AssetEs.SAFETY == risk) {
                assetsWarehouseVo.setSafety(searchRiskAsset(risk));
            } else if (AssetEs.LOW_RISK == risk) {
                assetsWarehouseVo.setLowRisk(searchRiskAsset(risk));
            } else if (AssetEs.MIDDLE_RISK == risk) {
                assetsWarehouseVo.setMiddleRisk(searchRiskAsset(risk));
            } else if (AssetEs.HIGH_RISK == risk) {
                assetsWarehouseVo.setHighRisk(searchRiskAsset(risk));
            }
        }
        List<GatherAssetEs> arrayList = new ArrayList<>();
        Iterable<GatherAssetEs> all = this.assetRepository.findAll();
        for (GatherAssetEs assetEs : all) {
            arrayList.add(assetEs);
        }
        assetsWarehouseVo.setAll(arrayList.size());
        return assetsWarehouseVo;
    }

    @SneakyThrows
    @Override
    public void exportData(Map<String, List<String>> ids, HttpServletResponse response,String name) {
        ArrayList<Long> idList = new ArrayList<>();
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment;filename=" + "file.xls");
            List<Asset> listGatherAssetEs = null;
            if ("all".equals(ids.get("ids").get(0))) {
                listGatherAssetEs = assetRep.findAll();
            } else {
                for (Map.Entry<String, List<String>> stringListEntry : ids.entrySet()) {
                    if ("ids".equals(stringListEntry.getKey())) {
                        List<Long> codesInteger = stringListEntry.getValue().stream().map(Long::parseLong).collect(Collectors.toList());
                        idList.addAll(codesInteger);
                    }
                }
                listGatherAssetEs = assetRep.findAllById(idList);
            }
            List<Asset> assetEsList = new ArrayList<>(listGatherAssetEs);
            assetEsList.sort(new Comparator<Asset>() {
                @Override
                public int compare(Asset assetEs, Asset t1) {
                    return assetEs.getIp().compareTo(t1.getIp());
                }
            });
            List<List<String>> hostComputerData = new ArrayList<List<String>>();
            OK:
            for (Asset asset : assetEsList) {
                List<String> list = new ArrayList<>();
                for (List<String> hostComputerDatum : hostComputerData) {
                    if (hostComputerDatum.contains(asset.getIp())) {
                        continue OK;
                    }
                }
                list.add(asset.getIp());
                list.add(asset.getName());
                list.add(asset.getAssetSysType() == null ? "" : asset.getAssetType().getName());
                List<AssetTag> collect = new ArrayList<>(asset.getAssetTags());
                list.add(asset.getAssetTags().isEmpty() ? "" : collect.get(0).getName());
                list.add(DictToName(asset.getAssetStatus()));
                list.add(asset.getDept() == null ? "" : asset.getDept().getName());
                String dateString = DateUtils.localDateTimeFormat(DateUtils.toLocalDateTime(asset.getUpdateTime()), DateUtils.DFY_MD_HMS);
                list.add(dateString);
                hostComputerData.add(list);
            }
            List<List<String>> applicationsData = new ArrayList<List<String>>();
            for (Asset asset : assetEsList) {
                List<AssetTag> collect = new ArrayList<>(asset.getAssetTags());
                ArrayList<String> list = new ArrayList<>();
                list.add(asset.getIp());
                list.add(String.valueOf(asset.getPort()));
                list.add(asset.getAssetExtend() == null ? "" : asset.getAssetExtend().getServer());
                list.add(asset.getAssetExtend() == null ? "" : asset.getAssetExtend().getServerComponent());
                list.add(DictToName(asset.getAssetStatus()));
                list.add(asset.getName());
                list.add(asset.getAssetSysType() == null ? "" : asset.getAssetSysType().getName());
                list.add(asset.getAssetTags().isEmpty() ? "" : collect.get(0).getName());
                list.add(asset.getDept() == null ? "" : asset.getDept().getName());
                list.add(asset.getAssetExtend() == null ? "" : asset.getAssetExtend().getTitle());
                list.add(asset.getAssetExtend() == null ? "" : asset.getAssetExtend().getWebsite());
                applicationsData.add(list);
            }
            String[] hostComputerAsset = {"??????IP", "????????????", "????????????", "????????????", "????????????", "????????????", "??????????????????"};
            String[] applicationsAsset = {"??????IP", "??????", "??????", "????????????", "????????????", "????????????", "????????????", "????????????", "????????????", "??????", "??????"};
            HSSFWorkbook workbook = new HSSFWorkbook();
            FileUtils.exportExcel(workbook, 0, "????????????", hostComputerAsset, hostComputerData, true);
            FileUtils.exportExcel(workbook, 1, "????????????", applicationsAsset, applicationsData, true);
            OutputStream os = response.getOutputStream();
            workbook.write(os);
            detailedFileService.saveDetailedFile(DetailedFileTypeEnum.VIEW_LIST, name, FileConstant.EXCEL_SUFFIX_XLS, workbook);
            // ????????????Servlet???3
            IoUtil.close(os);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ??????????????????
     *
     * @param asset            ??????
     * @param builder          builder
     * @param boolQueryBuilder ??????????????????
     */
    private void findConditions(AssetsWarehouseVo asset,
                                SearchSourceBuilder builder,
                                BoolQueryBuilder boolQueryBuilder) {
        if (null == asset.getPort() && null == asset.getName() && null == asset.getDept()
                && null == asset.getAssetStatus() && null == asset.getAssetTags() && null == asset.getAssetType()
                && null == asset.getStartTime() && null == asset.getEndTime() && null == asset.getFingerprint()
                && null == asset.getRiskLevel() && null == asset.getIp() && null == asset.getMore()) {
            if (null != asset.getAbnormalType() || null != asset.getWebAddress()) {
                boolQueryBuilder.must(QueryBuilders.termQuery("diff", 1));
            } else {
                boolQueryBuilder.must(QueryBuilders.termQuery("diff", 0));
            }
        }
        // ip
        if (null != asset.getIp()) {
            boolQueryBuilder.must(QueryBuilders.wildcardQuery("ip", "*" + asset.getIp() + "*"));
        }
        // ??????
        if (null != asset.getPort()) {
            boolQueryBuilder.must(QueryBuilders.wildcardQuery("port", "*" + asset.getPort() + "*"));
        }
        // ????????????
        if (null != asset.getAssetType()) {
            boolQueryBuilder.must(QueryBuilders.termsQuery("assetType", asset.getAssetType()));
        }
        // ????????????
        if (null != asset.getAssetTags()) {
            boolQueryBuilder.must(QueryBuilders.termsQuery("assetTags", asset.getAssetTags()));
        }
        // ????????????
        if (null != asset.getDept()) {
            boolQueryBuilder.must(QueryBuilders.termsQuery("dept", asset.getDept()));
        }
        // ????????????
        if (null != asset.getAssetStatus()) {
            boolQueryBuilder.must(QueryBuilders.termsQuery("assetStatus", asset.getAssetStatus()));
        }
        // ????????????
        if (null != asset.getAbnormalType()) {
            boolQueryBuilder.must(QueryBuilders.termsQuery("abnormalType", asset.getAbnormalType()));
        }
        // ????????????
        if (null != asset.getRiskLevel()) {
            boolQueryBuilder.must(QueryBuilders.termsQuery("riskLevel", asset.getRiskLevel()));
        }
        // ????????????
        if (null != asset.getName()) {
            boolQueryBuilder.must(QueryBuilders.wildcardQuery("name", "*" + asset.getName() + "*"));
        }
        // web ??????
        if (null != asset.getWebAddress()) {
            boolQueryBuilder.must(QueryBuilders.wildcardQuery("webAddress", "*" + asset.getWebAddress() + "*"));
        }
        // ????????????
        if (null != asset.getFingerprint()) {
            boolQueryBuilder.must(QueryBuilders.wildcardQuery("fingerprint", "*" + asset.getFingerprint() + "*"));
        }
        //????????????
        if (null != asset.getStartTime() && null != asset.getEndTime()) {
            RangeQueryBuilder updateDate = QueryBuilders.rangeQuery("updateTime")
                    .gte(asset.getStartTime())
                    .lte(asset.getEndTime());
            boolQueryBuilder.must(updateDate);
        }
        // ??????
        if (null != asset.getField()) {
            builder.sort(asset.getField(), SortOrder.fromString(asset.getOrder()))
                    .sort("utime", SortOrder.DESC);
        }
        if (null != asset.getMore()) {
            BoolQueryBuilder ip = QueryBuilders.boolQuery().must(QueryBuilders.wildcardQuery("ip", "*" + asset.getMore() + "*"));
            BoolQueryBuilder name = QueryBuilders.boolQuery().must(QueryBuilders.wildcardQuery("name", "*" + asset.getMore() + "*"));
            BoolQueryBuilder port = QueryBuilders.boolQuery().must(QueryBuilders.wildcardQuery("port", "*" + asset.getMore() + "*"));
            boolQueryBuilder.should(name).should(ip).should(port);
        }
    }

    /**
     * ??????????????????
     *
     * @param start ????????????
     * @return ??????
     */
    private Integer searchRiskAsset(Integer start) {
        CriteriaQuery criteriaQuery = new CriteriaQuery(new Criteria()
                .and(new Criteria("riskLevel").is(start)));
        SearchHits<AssetEs> safetyCount = this.elasticsearchRestTemplate.search(criteriaQuery, AssetEs.class);
        return Long.valueOf(safetyCount.getTotalHits()).intValue();
    }

    /**
     * ??????????????????
     *
     * @param start ????????????
     * @return ??????
     */
    private Integer searchStartAsset(Integer start) {
        CriteriaQuery criteriaQuery = new CriteriaQuery(new Criteria()
                .and(new Criteria("assetStatus").is(start)));
        SearchHits<AssetEs> safetyCount = this.elasticsearchRestTemplate.search(criteriaQuery, AssetEs.class);
        return Long.valueOf(safetyCount.getTotalHits()).intValue();
    }

    /**
     * ????????????
     *
     * @param risk ??????
     * @return ??????
     */
    private String DictToName(Integer risk) {
        if (risk == 0) return "??????";
        if (risk == 1) return "??????";
        if (risk == 2) return "??????";
        return null;
    }
}
