package com.cumulus.modules.business.controller;

import java.util.List;
import javax.annotation.Resource;
import com.cumulus.modules.business.dto.AssetDto;
import com.cumulus.modules.business.dto.AssetQueryCriteria;
import com.cumulus.modules.business.dto.BatchPackage;
import com.cumulus.modules.business.service.AssetService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 资产控制层
 *
 * @author zhangxq
 */
@RestController
@RequestMapping("/api/asset")
public class AssetController {

    /**
     * 资产服务接口
     */
    @Resource
    private AssetService assetService;

    /**
     * 复杂分页查询列表 父资产
     *
     * @param asset    查询条件
     * @param pageable 分页条件
     * @return 分页对象
     */
    @PostMapping("warehouse/findList")
    @PreAuthorize("@auth.check('assetWarehouse')")
    public ResponseEntity<Object> findList(@RequestBody AssetQueryCriteria asset, Pageable pageable) {
        return new ResponseEntity<>(assetService.findList(asset, pageable), HttpStatus.OK);
    }

    /**
     * 复杂分页查询列表 子资产
     *
     * @param asset    查询条件
     * @param pageable 分页条件
     * @return 分页对象
     */
    @PostMapping("warehouse/findChildList")
    @PreAuthorize("@auth.check('assetWarehouse')")
    public ResponseEntity<Object> findChildList(@RequestBody AssetQueryCriteria asset, Pageable pageable) {
        return new ResponseEntity<>(assetService.findChildList(asset, pageable), HttpStatus.OK);
    }


    /**
     * 查询全部资产计数 （全部 安全 低位 中危 高危 存活 下线 异常）
     *
     * @return map对象
     */
    @GetMapping("warehouse/assetCount")
    @PreAuthorize("@auth.check('assetWarehouse')")
    public ResponseEntity<Object> assetCount() {
        return new ResponseEntity<>(assetService.assetCount(), HttpStatus.OK);
    }

    /**
     * 分页查询资产
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return 资产列表
     */
    @GetMapping("/query")
    @PreAuthorize("@auth.check('assetList')")
    public ResponseEntity<Object> queryAll(AssetQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(assetService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    /**
     * 查询子资产
     *
     * @param pid      父id
     * @param pageable 分页参数
     * @return 资产列表
     */
    @GetMapping("/queryChild")
    @PreAuthorize("@auth.check('assetList')")
    public ResponseEntity<Object> queryChild(Long pid, Pageable pageable) {
        return new ResponseEntity<>(assetService.queryChild(pid, pageable), HttpStatus.OK);
    }

    /**
     * 查询ip相关信息
     *
     * @param ip
     * @return 资产列表
     */
    @GetMapping("/queryIp")
    @PreAuthorize("@auth.check('assetList')")
    public ResponseEntity<Object> queryByIp(@RequestParam String ip) {
        return new ResponseEntity<>(assetService.queryByCompleteIp(ip), HttpStatus.OK);
    }

    /**
     * 资产清单 新增
     *
     * @param assetDto 资产
     * @return 结果
     */
    @PostMapping("/create")
    @PreAuthorize("@auth.check('assetList')")
    public ResponseEntity<Object> create(@RequestBody AssetDto assetDto) {
        assetService.create(assetDto, false);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 确认资产 录入资产
     *
     * @param assetDto 资产
     * @return 结果
     */
    @PostMapping("/createByConfirm")
    @PreAuthorize("@auth.check('assetList')")
    public ResponseEntity<Object> createByAssetConfirm(@RequestBody AssetDto assetDto) {
        assetService.createByAssetConfirm(assetDto, false);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 资产清单 批量新增资产
     *
     * @param file 资产传输对象列表
     * @return 资产传输对象列表
     */
    @PostMapping("/createBatch")
    @PreAuthorize("@auth.check('assetList')")
    public ResponseEntity<Object> createBatch(MultipartFile file) {
        return new ResponseEntity<>(assetService.createBatch(file, true), HttpStatus.OK);
    }

    /**
     * 确认资产 批量新增资产
     *
     * @param file 资产传输对象列表
     * @return 资产传输对象列表
     */
    @PostMapping("/createBatchByConfirm")
    @PreAuthorize("@auth.check('assetList')")
    public ResponseEntity<Object> createBatchByConfirm(MultipartFile file) {
        return new ResponseEntity<>(assetService.createBatch(file, false), HttpStatus.OK);
    }

    /**
     * 批量登录测试
     *
     * @param batchPackage
     * @return 资产传输对象列表
     */
    @PostMapping("/loginTest")
    @PreAuthorize("@auth.check('assetList')")
    public ResponseEntity<Object> loginTest(@RequestBody BatchPackage batchPackage) {
        return new ResponseEntity<>(assetService.loginTest(batchPackage.getIds(), batchPackage.isAll()), HttpStatus.OK);
    }

    /**
     * 修改资产
     *
     * @param assetDtos 资产传输对象
     * @return 结果
     */
    @PutMapping("/update")
    @PreAuthorize("@auth.check('assetList')")
    public ResponseEntity<Object> update(@RequestBody List<AssetDto> assetDtos) {
        assetService.updateById(assetDtos);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 删除资产
     *
     * @param id 资产主键
     * @return 结果
     */
    @DeleteMapping("/del")
    @PreAuthorize("@auth.check('assetList')")
    public ResponseEntity<Object> delete(Long id) {
        assetService.removeById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 批量删除资产
     *
     * @param batchPackage 资产列表
     * @return 结果
     */
    @DeleteMapping("/delBatch")
    @PreAuthorize("@auth.check('assetList')")
    public ResponseEntity<Object> deleteBatch(@RequestBody BatchPackage batchPackage) {
        assetService.removeBatch(batchPackage.getIds(), batchPackage.isAll());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 检查名称
     *
     * @param name 新建任务名称
     * @return true 不重复 false 重复
     */
    @GetMapping("/checkName")
    @PreAuthorize("@auth.check('assetList')")
    public ResponseEntity<Object> check(@RequestParam String name) {
        return new ResponseEntity<>(assetService.checkName(name), HttpStatus.OK);
    }

}
