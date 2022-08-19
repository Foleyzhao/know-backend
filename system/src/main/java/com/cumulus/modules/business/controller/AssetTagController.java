package com.cumulus.modules.business.controller;

import javax.annotation.Resource;

import com.cumulus.modules.business.dto.BatchPackage;
import com.cumulus.modules.business.entity.AssetTag;
import com.cumulus.modules.business.service.AssetTagService;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 资产标签控制层
 *
 * @author zhangxq
 */
@Slf4j
@RestController
@RequestMapping("/api/assetTag")
@PreAuthorize("@auth.check('autonomousConfiguration')")
public class AssetTagController {


    /**
     * 资产标签服务接口
     */
    @Resource
    private AssetTagService assetTagService;


    /**
     * 分页查询资产标签
     *
     * @param pageable 分页参数
     * @return 资产标签列表
     */
    @GetMapping("/query")
    public ResponseEntity<Object> queryAll(Pageable pageable) {
        return new ResponseEntity<>(assetTagService.queryAll(pageable), HttpStatus.OK);
    }

    /**
     * 分页查询资产标签
     *
     * @param pageable 分页参数
     * @return 资产标签列表
     */
    @GetMapping("/queryTreePage")
    public ResponseEntity<Object> queryTreePage(Pageable pageable) {
        return new ResponseEntity<>(assetTagService.queryTreePage(pageable), HttpStatus.OK);
    }

    /**
     * 分页查询资产标签
     *
     * @param parentTagId 父类标签id
     * @param pageable    分页参数
     * @return 资产标签列表
     */
    @GetMapping("/queryTreePageSub")
    public ResponseEntity<Object> queryTreePageSub(Long parentTagId, Pageable pageable) {
        return new ResponseEntity<>(assetTagService.queryTreePageSub(parentTagId, pageable), HttpStatus.OK);
    }

    /**
     * 分页查询资产标签
     *
     * @return 资产标签列表
     */
    @GetMapping("/querySelect")
    @PreAuthorize("@auth.check('autonomousConfiguration', 'assetWarehouse', 'recognitionAssets', 'assetList')")
    public ResponseEntity<Object> queryAll() {
        return new ResponseEntity<>(assetTagService.querySelect(), HttpStatus.OK);
    }

    /**
     * 新增资产标签
     *
     * @param assetTag 资产标签列表
     * @return 资产标签列表
     */
    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody AssetTag assetTag) {
        assetTagService.create(assetTag);
        return new ResponseEntity<>("新增成功", HttpStatus.OK);
    }

    /**
     * 新增资产标签
     *
     * @param file excel文件
     * @return 资产标签列表
     */
    @PostMapping("/createBatch")
    public ResponseEntity<Object> create(MultipartFile file) {
        return new ResponseEntity<>(assetTagService.createBatch(file), HttpStatus.OK);
    }

    /**
     * 修改资产标签
     *
     * @param assetTag 资产标签传输对象
     * @return 资产标签传输对象
     */
    @PutMapping("/update")
    public ResponseEntity<Object> update(@RequestBody AssetTag assetTag) {
        assetTagService.updateById(assetTag);
        return new ResponseEntity<>("更新成功", HttpStatus.OK);
    }

    /**
     * 删除资产标签
     *
     * @param id 资产标签主键
     * @return 资产标签传输对象
     */
    @DeleteMapping("/del")
    public ResponseEntity<Object> delete(Long id) {
        assetTagService.removeById(id);
        return new ResponseEntity<>("删除成功", HttpStatus.OK);
    }

    /**
     * 批量删除资产标签
     *
     * @param batchPackage 删除参数包装
     * @return 资产标签列表
     */
    @DeleteMapping("/delBatch")
    public ResponseEntity<Object> deleteBatch(@RequestBody BatchPackage batchPackage) {
        return new ResponseEntity<>(assetTagService.removeBatch(batchPackage.getIds(), batchPackage.isAll()), HttpStatus.OK);
    }


}
