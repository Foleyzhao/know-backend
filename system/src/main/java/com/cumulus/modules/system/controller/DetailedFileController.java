package com.cumulus.modules.system.controller;

import com.cumulus.exception.BadRequestException;
import com.cumulus.modules.system.entity.DetailedFile;
import com.cumulus.modules.system.mapstruct.DetailedFileMapper;
import com.cumulus.modules.system.repository.DetailedFileRepository;
import com.cumulus.modules.system.service.DetailedFileService;
import com.cumulus.utils.CommonUtils;
import com.cumulus.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 明细清单控制层
 *
 * @author : shenjc
 */
@RestController
@RequestMapping("/api/detailedFile")
@PreAuthorize("@auth.check('statisticalReports')")
public class DetailedFileController {

    /**
     * 明细清单数据接口
     */
    @Autowired
    private DetailedFileRepository detailedFileRepository;

    /**
     * 明细清单数据接口
     */
    @Autowired
    private DetailedFileService detailedFileService;

    /**
     * 明细清单Mapper
     */
    @Autowired
    private DetailedFileMapper detailedFileMapper;


    /**
     * 根据参数查询明细清单
     *
     * @param pageable 分页参数
     * @param params   查询参数
     * @return 明细清单分页
     */
    @PostMapping
    public ResponseEntity<?> page(Pageable pageable, @RequestBody Map<String, String> params) {
        return new ResponseEntity<>(detailedFileService.page(pageable, CommonUtils.mapToMultiValueMap(params))
                .map(detailedFileMapper::toDto), HttpStatus.OK);
    }


    /**
     * 下载明细清单文件
     *
     * @param response       请求响应
     * @param request        请求
     * @param detailedFileId 明细清单id
     * @return 字典详情列表
     */
    @PostMapping("downloadDetailedFile")
    public ResponseEntity<?> downloadDetailedFile(Long detailedFileId, HttpServletRequest request, HttpServletResponse response) {
        Optional<DetailedFile> detailedFileOpt = detailedFileRepository.findById(detailedFileId);
        if (!detailedFileOpt.isPresent()) {
            throw new BadRequestException("文件不存在");
        }
        DetailedFile detailedFile = detailedFileOpt.get();
        String path = detailedFileService.getDetailedFilePathById(detailedFileId);
        FileUtils.downloadFile(new File(path), response, request,
                detailedFile.getName() + detailedFile.getFileSuffix());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 单个删除明细清单
     *
     * @param id 明细清单id
     * @return 请求响应
     */
    @DeleteMapping(path = "/delete/{id}")
    public ResponseEntity<?> deleteScanPlan(@PathVariable("id") Long id) {
        detailedFileService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 批量删除明细清单
     *
     * @param list id列表
     * @return 请求响应
     */
    @PostMapping(path = "/deleteBatches")
    public ResponseEntity<?> deleteBatches(@RequestBody List<Long> list) {
        if (list.isEmpty()) {
            throw new BadRequestException("漏洞扫描计划,批量删除失败");
        }
        return new ResponseEntity<>(detailedFileService.deleteBatches(list), HttpStatus.OK);
    }

    /**
     * 批量删除扫描计划
     *
     * @return 请求响应
     */
    @PostMapping(path = "/deleteAll")
    public ResponseEntity<?> deleteAll() {
        final List<Long> idList = detailedFileRepository.findAll().stream().map(DetailedFile::getId).collect(Collectors.toList());
        return new ResponseEntity<>(detailedFileService.deleteBatches(idList), HttpStatus.OK);
    }
}
