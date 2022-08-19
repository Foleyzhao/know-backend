package com.cumulus.modules.business.gather.controller;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.service.AssetEsService;
import com.cumulus.modules.business.gather.service.BasicInfoEsService;
import com.cumulus.modules.business.gather.vo.AssetsWarehouseVo;
import com.cumulus.modules.business.other.service.FlawEsService;
import com.cumulus.modules.business.service.AssetService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 资产控制层
 *
 * @author Shijh
 */
@RestController
@RequestMapping("api/es/asset/")
@PreAuthorize("@auth.check('assetWarehouse')")
public class AssetEsController {

    /**
     * 资产es接口服务
     */
    @Autowired
    private AssetEsService assetEsService;

    /**
     * 资产服务接口
     */
    @Autowired
    private AssetService assetService;

    /**
     * 风险接口服务
     */
    @Autowired
    private FlawEsService flawEsService;

    /**
     * 基本信息Es服务
     */
    @Autowired
    private BasicInfoEsService basicInfoEsService;

    /**
     * 批量添加
     *
     * @param assets 参数列表
     * @return 结果集
     */
    @PostMapping("saveBatch")
    public ResponseEntity<Object> saveBatch(@RequestBody List<GatherAssetEs> assets) {
        if (CollectionUtils.isEmpty(assets)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.assetEsService.saveAll(assets);
        return new ResponseEntity<>("新增成功", HttpStatus.CREATED);
    }

    /**
     * 根据id删除
     *
     * @param id id
     * @return 结果集
     */
    @PostMapping("deleteById")
    public ResponseEntity<Object> deleteById(@RequestParam String id) {
        this.assetEsService.deleteById(id);
        return new ResponseEntity<>("删除成功", HttpStatus.OK);
    }

    /**
     * 根据id更新
     *
     * @param assetEs 要修改的
     * @return 结果集
     */
    @PostMapping("updateById")
    public ResponseEntity<Object> updateById(@RequestBody GatherAssetEs assetEs) {
        this.assetEsService.updateById(assetEs);
        return new ResponseEntity<>("更新成功", HttpStatus.OK);
    }

    /**
     * 详情
     *
     * @param assetId 资产id
     * @return 结果集
     */
    @PostMapping("findById")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Object> findById(Long assetId) {
        return new ResponseEntity<>(assetService.findGatherById(assetId), HttpStatus.OK);
    }

    /**
     * 通过资产ip查询端口资产
     *
     * @param ip 要查询资产ip下的端口资产
     * @return 结果集
     */
    @GetMapping("findByPortAsset")
    public ResponseEntity<Object> findByPortAsset(String ip, Pageable pageable, @RequestBody AssetsWarehouseVo assetsWarehouseVo) {
        return new ResponseEntity<>(this.assetEsService.findByPortAsset(ip, pageable, assetsWarehouseVo), HttpStatus.OK);
    }

    /**
     * 统计资产状态,风险状态
     *
     * @return 结果集
     */
    @GetMapping("countAssetStartAndRisk")
    public ResponseEntity<Object> countAssetStart() {
        return new ResponseEntity<>(this.assetEsService.countAssetStart(), HttpStatus.OK);
    }

    /**
     * 导出excel文件
     *
     * @param ids      批量导出
     * @param response 相应
     */
    @PostMapping("exportData")
    public void exportData(@RequestBody Map<String, List<String>> ids, HttpServletResponse response, String name) {
        this.assetEsService.exportData(ids, response, name);
    }

    /**
     * 获取资产ip下的漏洞信息
     *
     * @param ip 资产ip
     * @return 结果集
     */
    @PostMapping("getFlawInformation")
    public ResponseEntity<Object> getFlawInformation(@RequestParam String ip, Pageable pageable) {
        return new ResponseEntity<>(this.flawEsService.getFlawInformation(ip, pageable), HttpStatus.OK);
    }

    /**
     * 获取基本信息分页接口
     */
    @GetMapping("getBasicInfo")
    public ResponseEntity<Object> findList(String id, Pageable pageable) {
        return new ResponseEntity<>(basicInfoEsService.findListRecent(id, pageable), HttpStatus.OK);
    }

    /**
     * 资产画像展示的Tab页
     */
    @GetMapping("getHeader")
    public ResponseEntity<Object> getHeader(Long assetId) {
        return new ResponseEntity<>(assetService.getHeader(assetId),HttpStatus.OK);
    }
}
