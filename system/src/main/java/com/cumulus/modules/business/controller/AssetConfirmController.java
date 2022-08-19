package com.cumulus.modules.business.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.cumulus.modules.business.dto.AssetConfirmQueryCriteria;
import com.cumulus.modules.business.dto.BatchPackage;
import com.cumulus.modules.business.service.AssetConfirmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 确认资产控制层
 *
 * @author zhangxq
 */
@RestController
@RequestMapping("/api/assetConfirm")
@PreAuthorize("@auth.check('recognitionAssets')")
public class AssetConfirmController {

    /**
     * 确认资产服务接口
     */
    @Autowired
    private AssetConfirmService assetConfirmService;

    /**
     * 分页查询确认资产
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return 资产列表
     */
    @GetMapping("/query")
    public ResponseEntity<Object> queryAll(AssetConfirmQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(assetConfirmService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    /**
     * 删除确认资产
     *
     * @param id id
     * @return
     */
    @DeleteMapping("/del")
    public ResponseEntity<Object> delete(Long id) {
        assetConfirmService.removeById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 批量删除资产
     *
     * @param batchPackage 删除参数包装
     * @return 资产列表
     */
    @DeleteMapping("/delBatch")
    public ResponseEntity<Object> deleteBatch(@RequestBody BatchPackage batchPackage) {
        assetConfirmService.removeBatch(batchPackage.getIds(), batchPackage.isAll());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 导出 导入模板 zip
     *
     * @param batchPackage
     * @param response
     */
    @PostMapping("/exportZip")
    public void exportZip(@RequestBody BatchPackage batchPackage, HttpServletResponse response,
                          HttpServletRequest request) {
        assetConfirmService.exportZip(batchPackage.getIds(), batchPackage.isAll(), response, request);
    }

    /**
     * 导出 导入模板 excel
     *
     * @param batchPackage
     * @param response
     */
    @PostMapping("/export")
    public void export(@RequestBody BatchPackage batchPackage, HttpServletResponse response) {
        assetConfirmService.exportExcel(batchPackage.getIds(), batchPackage.isAll(), response);
    }

    /**
     * 根据ip查找端口资产
     *
     * @param ip ip
     * @return 端口资产
     */
    @GetMapping("/getPorts")
    public ResponseEntity<Object> getByIp(Pageable pageable, String ip) {
        return new ResponseEntity<>(assetConfirmService.getByIp(pageable, ip), HttpStatus.OK);
    }

    /**
     * 单个确认
     *
     * @param id
     * @return
     */
    @GetMapping("/singleConfirm")
    public ResponseEntity<Object> singleConfirm(Long id) {
        assetConfirmService.singleConfirm(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 批量确认
     *
     * @param batchPackage
     * @return
     */
    @PostMapping("/batchConfirm")
    public ResponseEntity<Object> batchConfirm(@RequestBody BatchPackage batchPackage) {
        assetConfirmService.batchConfirm(batchPackage.getIds(), batchPackage.isAll());
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
