package com.cumulus.modules.business.controller;

import javax.annotation.Resource;
import com.cumulus.modules.business.repository.AssetRepository;
import com.cumulus.modules.business.service.AssetService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 安全大屏
 *
 * @author Shijh
 */
@RestController
@RequestMapping("/api/LargeScreen")
public class SafetyLargeScreenController {

    /**
     * 资产数据访问接口
     */
    @Resource
    private AssetRepository assetRepository;

    /**
     * 资产服务接口
     */
    @Resource
    private AssetService assetService;


    /**
     * 数据分析
     *
     * @return 结果集
     */
    @GetMapping("/findAssetSum")
//    @AnonymousGetMapping("/findAssetSum")
    public ResponseEntity<Object> findAssetSum() {
        return new ResponseEntity<>(assetService.findAssetSum(), HttpStatus.OK);
    }

    /**
     * 主机数-端口数-网站数
     *
     * @return 结果集
     */
    @GetMapping("/findAssetNum")
//    @AnonymousGetMapping("/findAssetNum")
    public ResponseEntity<Object> findAssetNum() {
        return new ResponseEntity<>(assetService.findAssetNum(), HttpStatus.OK);
    }

    /**
     * 高频漏洞
     *
     * @return 结果集
     */
    @GetMapping("/findHighLeak")
//    @AnonymousGetMapping("/findHighLeak")
    public ResponseEntity<Object> findHighLeak() {
        return new ResponseEntity<>(assetService.findHighLeak(), HttpStatus.OK);
    }

    /**
     * 风险趋势
     *
     * @return 结果集
     */
    @GetMapping("/findRiskTrend")
//    @AnonymousGetMapping("/findRiskTrend")
    public ResponseEntity<Object> findRiskTrend() {
        return new ResponseEntity<>(assetService.findRiskTrend(), HttpStatus.OK);
    }

    /**
     * 资产变更趋势
     *
     * @return 结果集
     */
    @GetMapping("/findAssetUpdateTrend")
//    @AnonymousGetMapping("/findAssetUpdateTrend")
    public ResponseEntity<Object> findAssetUpdateTrend() {
        return new ResponseEntity<>(assetService.findAssetUpdateTrend(), HttpStatus.OK);
    }

    /**
     * 实时告警
     *
     * @return 结果集
     */
    @GetMapping("/findRealtimeAlarm")
//    @AnonymousGetMapping("/findRealtimeAlarm")
    public ResponseEntity<Object> findRealtimeAlarm() {
        return new ResponseEntity<>(assetService.findRealtimeAlarm(), HttpStatus.OK);
    }

    /**
     * 部门
     *
     * @return 结果集
     */
    @GetMapping("/findDeptNum")
//    @AnonymousGetMapping("/findDeptNum")
    public ResponseEntity<Object> findDeptNum() {
        return new ResponseEntity<>(assetService.findDeptNum(), HttpStatus.OK);
    }

    /**
     * 主机服务TOP5
     *
     * @return 结果集
     */
    @GetMapping("/findSys")
//    @AnonymousGetMapping("/findSys")
    public ResponseEntity<Object> findSys() {
        return new ResponseEntity<>(assetRepository.findSys(), HttpStatus.OK);
    }

    /**
     * 操作系统TOP5
     *
     * @return 结果集
     */
    @GetMapping("/findServer")
//    @AnonymousGetMapping("/findServer")
    public ResponseEntity<Object> findServer() {
        return new ResponseEntity<>(assetRepository.findServer(), HttpStatus.OK);
    }

    /**
     * 资产风险概况
     *
     * @return 结果集
     */
    @GetMapping("/findRisk")
//    @AnonymousGetMapping("/findRisk")
    public ResponseEntity<Object> findRisk() {
        return new ResponseEntity<>(assetService.findRisk(), HttpStatus.OK);
    }

        /**
     * 地图统计
     *
     * @return 结果集
     */
    @GetMapping("/findMap")
//    @AnonymousGetMapping("/findMap")
    public ResponseEntity<Object> findMap() {
        return new ResponseEntity<>(assetService.findMap(), HttpStatus.OK);
    }
}