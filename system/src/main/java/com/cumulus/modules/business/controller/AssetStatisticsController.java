package com.cumulus.modules.business.controller;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.cumulus.modules.business.repository.AssetRepository;
import com.cumulus.modules.business.service.AssetService;
import com.cumulus.modules.business.service.impl.AssetServiceImpl;
import com.cumulus.modules.business.vulnerability.service.VulnerabilityEsService;
import com.cumulus.utils.DateUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 综控台
 *
 * @author Shijh
 */
@Slf4j
@RestController
@RequestMapping("/api/statistics")
@PreAuthorize("@auth.check('dataAnalysis')")
public class AssetStatisticsController {

    /**
     * 综控台获取数据的时间类型 1:24 小时 2：周 3：月 4：半年 5：全部
     */
    public static final int DATE_TYPE_DAY = 1;
    public static final int DATE_TYPE_WEEK = 2;
    public static final int DATE_TYPE_MONTH = 3;
    public static final int DATE_TYPE_HALF_YEAR = 4;
    public static final int DATE_TYPE_ALL = 5;

    /**
     * 资产数据访问接口
     */
    @Autowired
    private AssetRepository assetRepository;
    /**
     * 资产服务接口
     */
    @Autowired
    private AssetService assetService;
    /**
     * 漏洞服务接口
     */
    @Autowired
    private VulnerabilityEsService vulnerabilityEsService;
    /**
     * 漏洞服务接口
     */
    @Autowired
    private AssetServiceImpl assetServiceImpl;

    /**
     * 数据分析
     *
     * @return 结果集
     */
    @GetMapping("/findDataAnalysis")
    public ResponseEntity<Object> findDataAnalysis() {
        return new ResponseEntity<>(assetRepository.findDataAnalysis(), HttpStatus.OK);
    }

    /**
     * 部门分布
     *
     * @return 结果集
     */
    @GetMapping("/findDept")
    public ResponseEntity<Object> findDept() {
        return new ResponseEntity<>(assetRepository.findDept(), HttpStatus.OK);
    }

    /**
     * 应用资产-网络服务
     *
     * @return 结果集
     */
    @GetMapping("/findApplyAsset")
    public ResponseEntity<Object> findApplyAsset() {
        return new ResponseEntity<>(assetRepository.findApplyAsset(), HttpStatus.OK);
    }

    /**
     * 应用资产-程序框架
     *
     * @return 结果集
     */
    @GetMapping("/findProcedure")
    public ResponseEntity<Object> findProcedure() {
        return new ResponseEntity<>(assetRepository.findProcedure(), HttpStatus.OK);
    }

    /**
     * 主机资产-主机端口
     *
     * @return 结果集
     */
    @GetMapping("/findHost")
    public ResponseEntity<Object> findHost() {
        List<Map<String, Object>> host = assetRepository.findHost();
        List<Map<String, Object>> newHost = new ArrayList<>();
        for (Map<String, Object> stringStringMap : host) {
            if (stringStringMap.containsValue("其他")) {
                String num = String.valueOf(stringStringMap.get("num"));
                if (Integer.parseInt(num) == 0) {
                    continue;
                }
            }
            newHost.add(stringStringMap);
        }
        return new ResponseEntity<>(newHost, HttpStatus.OK);
    }

    /**
     * 主机资产-主机服务
     *
     * @return 结果集
     */
    @GetMapping("/findHostServer")
    public ResponseEntity<Object> findHostServer() {
        List<Map<String, Object>> hostServer = assetRepository.findHostServer();
        List<Map<String, Object>> newHostServer = new ArrayList<>();
        for (Map<String, Object> stringStringMap : hostServer) {
            if (stringStringMap.containsValue("其他")) {
                String num = String.valueOf(stringStringMap.get("num"));
                if (Integer.parseInt(num) == 0) {
                    continue;
                }
            }
            newHostServer.add(stringStringMap);
        }
        return new ResponseEntity<>(newHostServer, HttpStatus.OK);
    }

    /**
     * 主机资产-操作系统
     *
     * @return 结果集
     */
    @GetMapping("/findSystem")
    public ResponseEntity<Object> findSystem() {
        HashMap<String, Object> otherMap = new HashMap<>();
        List<Map<String, Object>> system = assetRepository.findSystem();
        int all = assetRepository.countAllSystem();
        for (Map<String, Object> item : system) {
            BigInteger num = (BigInteger) item.get("num");
            all -= num.intValue();
        }
        if (all != 0) {
            otherMap.put("name", "其他");
            otherMap.put("num", all);
            system.add(otherMap);
        }
        return new ResponseEntity<>(system, HttpStatus.OK);
    }


    /**
     * 资产概况-类型分布
     *
     * @return 结果集
     */
    @GetMapping("/findAssetType")
    public ResponseEntity<Object> findAssetType() {
        return new ResponseEntity<>(assetRepository.findAssetType(), HttpStatus.OK);
    }

    /**
     * 资产概况-标签分布
     *
     * @return 结果集
     */
    @GetMapping("/findAssetTag")
    public ResponseEntity<Object> findAssetTag() {
        return new ResponseEntity<>(assetRepository.findAssetTag(), HttpStatus.OK);
    }

    /**
     * 资产风险概况
     *
     * @return 结果集
     */
    @GetMapping("/countRisk")
    public ResponseEntity<Object> countRisk() {
        return new ResponseEntity<>(assetRepository.countRisk(), HttpStatus.OK);
    }

    /**
     * 资产风险趋势
     *
     * @return 结果集
     */
    @GetMapping("/countRiskDetail")
    public ResponseEntity<Object> countRiskDetail() {
        List<Map<String, Object>> maps = new ArrayList<>();
        List<Map<String, Object>> riskTrend = assetRepository.countRiskDetail();
        for (Map<String, Object> objectObjectMap : riskTrend) {
            Map<String, Object> map = new HashMap<>();
            for (Map.Entry<String, Object> objectObjectEntry : objectObjectMap.entrySet()) {
                if ("click_date".equals(objectObjectEntry.getKey())) {
                    map.put(objectObjectEntry.getKey(), objectObjectEntry.getValue().toString().substring(6));
                    continue;
                }
                map.put(objectObjectEntry.getKey(), objectObjectEntry.getValue());
            }
            maps.add(map);
        }
        long countHigh = assetServiceImpl.getCountVulLatest(3);
        long countMiddle = assetServiceImpl.getCountVulLatest(2);
        long countLow = assetServiceImpl.getCountVulLatest(1);
        Map<String, Object> objectHash = new HashMap<>();
        objectHash.put("低危", countLow);
        objectHash.put("中危", countMiddle);
        objectHash.put("高危", countHigh);
        objectHash.put("click_date", DateUtils.localDateTimeFormatyMd(LocalDateTime.now()).substring(6));
        maps.add(objectHash);
        return new ResponseEntity<>(maps, HttpStatus.OK);
    }

    /**
     * 漏洞频次
     *
     * @param date 时间
     * @return 结果集
     */
    @GetMapping("/countVulNumber")
    public ResponseEntity<Object> countVulNumber(Integer date) {
        return new ResponseEntity<>(vulnerabilityEsService.countVulNumber(date), HttpStatus.OK);
    }

    /**
     * 资产变更
     *
     * @return 结果集
     */
    @GetMapping("/findAssetUpdate")
    public ResponseEntity<Object> findAssetUpdate() {
        return new ResponseEntity<>(assetService.findAssetUpdate(), HttpStatus.OK);
    }

    /**
     * 资产变更频次
     *
     * @param date 时间
     * @return 结果集
     */
    @GetMapping("/countAssetUpdate")
    public ResponseEntity<Object> countAssetUpdate(Integer date, Integer assetCategory) {
        return new ResponseEntity<>(assetService.countAssetUpdate(date, assetCategory), HttpStatus.OK);
    }

}
