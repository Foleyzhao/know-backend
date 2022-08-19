package com.cumulus.modules.business.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.cumulus.modules.business.dto.BatchPackage;
import com.cumulus.modules.business.dto.IpLibraryDto;
import com.cumulus.modules.business.dto.IpLibraryQueryCriteria;
import com.cumulus.modules.business.service.IpLibraryService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * ip库控制层
 *
 * @author zhangxq
 */
@RestController
@RequestMapping("/api/ipLibrary")
public class IpLibraryController {

    /**
     * ip库服务接口
     */
    @Resource
    private IpLibraryService ipLibraryService;

    /**
     * 分页查询ip
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return ip列表
     */
    @GetMapping("/query")
    public ResponseEntity<Object> queryAll(IpLibraryQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(ipLibraryService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    /**
     * 新增ip
     *
     * @param ipLibraryDto ip
     * @return 结果
     */
    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody IpLibraryDto ipLibraryDto) {
        ipLibraryService.createByIpRange(ipLibraryDto);
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    /**
     * 批量新增ip
     *
     * @param file ip列表
     * @return 导入结果
     */
    @PostMapping("/createBatch")
    public ResponseEntity<Object> createBatch(MultipartFile file) {
        return new ResponseEntity<>(ipLibraryService.createBatch(file), HttpStatus.OK);
    }

    /**
     * 修改ip库
     *
     * @param ipLibraryDto IP库传输对象
     * @return 结果
     */
    @PutMapping("/update")
    public ResponseEntity<Object> update(@RequestBody IpLibraryDto ipLibraryDto) {
        ipLibraryService.updateById(ipLibraryDto);
        return new ResponseEntity<>("", HttpStatus.OK);
    }


    /**
     * 删除ip
     *
     * @param id ip主键
     * @return 结果
     */
    @DeleteMapping("/del")
    public ResponseEntity<Object> delete(Long id) {
        ipLibraryService.removeById(id);
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    /**
     * 批量删除ip
     *
     * @param batchPackage id列表
     * @return 结果
     */
    @DeleteMapping("/delBatch")
    public ResponseEntity<Object> deleteBatch(@RequestBody BatchPackage batchPackage) {
        ipLibraryService.removeBatch(batchPackage.getIds(), batchPackage.isAll());
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    /**
     * 导出excel
     *
     * @return excel
     */
    @PostMapping("/export")
    public void export(@RequestBody BatchPackage batchPackage, HttpServletResponse response) {
        ipLibraryService.export(batchPackage.getIds(), batchPackage.isAll(), response);
    }
}
