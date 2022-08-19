package com.cumulus.modules.business.controller;

import javax.annotation.Resource;

import com.cumulus.modules.business.dto.AssetSysTypeDto;
import com.cumulus.modules.business.dto.AssetTypeDto;
import com.cumulus.modules.business.dto.BatchPackage;
import com.cumulus.modules.business.service.AssetTypeService;
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
 * 资产类型控制层
 *
 * @author zhangxq
 */
@RestController
@RequestMapping("/api/assetType")
@PreAuthorize("@auth.check('autonomousConfiguration')")
public class AssetTypeController {

    /**
     * 系统部门服务接口
     */
    @Resource
    private AssetTypeService assetTypeService;

    /**
     * 查询系统类型
     *
     * @param pageable 分页参数
     * @return 系统类型列表
     */
    @GetMapping("/query")
    @PreAuthorize("@auth.check('autonomousConfiguration', 'assetWarehouse')")
    public ResponseEntity<Object> queryAll(Pageable pageable) {
        return new ResponseEntity<>(assetTypeService.queryAll(pageable), HttpStatus.OK);
    }

    /**
     * 查询所有类型
     *
     * @return 类型列表
     */
    @GetMapping("/querySelect")
    @PreAuthorize("@auth.check('autonomousConfiguration', 'recognitionAssets', 'assetList')")
    public ResponseEntity<Object> querySelect() {
        return new ResponseEntity<>(assetTypeService.querySelecct(), HttpStatus.OK);
    }

    /**
     * 查询系统类型
     *
     * @param pageable 分页参数
     * @return 系统类型列表
     */
    @GetMapping("/queryChild")
    public ResponseEntity<Object> queryChild(Integer id, Pageable pageable) {
        return new ResponseEntity<>(assetTypeService.queryChild(id, pageable), HttpStatus.OK);
    }

    /**
     * 新增资产类型
     *
     * @param assetSysTypeDto 子资产类型传输对象
     * @return
     */
    @PostMapping("/createChild")
    public ResponseEntity<Object> createChild(@RequestBody AssetSysTypeDto assetSysTypeDto) {
        assetTypeService.create(assetSysTypeDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 新增资产类型
     *
     * @param assetTypeDto 子资产类型传输对象
     * @return
     */
    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody AssetTypeDto assetTypeDto) {
        assetTypeService.create(assetTypeDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    /**
     * 批量新增资产类型
     *
     * @param file 资产类型传输对象列表
     * @return 资产类型传输对象列表
     */
    @PostMapping("/createBatch")
    public ResponseEntity<Object> createBatch(@RequestBody MultipartFile file) {
        return new ResponseEntity<>(assetTypeService.createBatch(file), HttpStatus.OK);
    }

    /**
     * 修改资产类型
     *
     * @param assetTypeDto 资产类型传输对象
     * @return 资产类型传输对象
     */
    @PutMapping("/update")
    public ResponseEntity<Object> update(@RequestBody AssetTypeDto assetTypeDto) {
        assetTypeService.updateById(assetTypeDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 修改资产类型
     *
     * @param assetSysTypeDto 子资产类型传输对象
     * @return 资产类型传输对象
     */
    @PutMapping("/updateChild")
    public ResponseEntity<Object> update(@RequestBody AssetSysTypeDto assetSysTypeDto) {
        assetTypeService.updateSysTypeById(assetSysTypeDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 删除资产类型
     *
     * @param id 资产类型主键
     * @return 资产类型传输对象
     */
    @DeleteMapping("/del")
    public ResponseEntity<Object> delete(@RequestParam Integer id) {
        assetTypeService.removeTypeById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 删除子资产类型
     *
     * @param id 资产类型主键
     * @return 资产类型传输对象
     */
    @DeleteMapping("/delChild")
    public ResponseEntity<Object> deleteChild(@RequestParam Integer id) {
        assetTypeService.removeSysTypeById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 批量删除资产类型
     *
     * @param batchPackageInt 删除参数包装类
     * @return 资产类型列表
     */
    @DeleteMapping("/delBatch")
    public ResponseEntity<Object> deleteBatch(@RequestBody BatchPackage.BatchPackageInt batchPackageInt) {
        assetTypeService.removeBatchType(batchPackageInt.getIds(), batchPackageInt.isAll());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 批量删除子资产类型
     *
     * @param batchPackageInt 删除参数包装类
     * @return 资产类型列表
     */
    @DeleteMapping("/delChildBatch")
    public ResponseEntity<Object> delChildBatch(@RequestBody BatchPackage.BatchPackageInt batchPackageInt) {
        assetTypeService.removeBatchSysType(batchPackageInt.getIds(), batchPackageInt.isAll());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 全部删除
     *
     * @return 删除结果
     */
    @DeleteMapping("/delAll")
    public ResponseEntity<Object> delAll() {
        assetTypeService.delAll();
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
