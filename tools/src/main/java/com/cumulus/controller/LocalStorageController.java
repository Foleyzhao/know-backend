package com.cumulus.controller;

import com.cumulus.annotation.Log;
import com.cumulus.dto.LocalStorageQueryCriteria;
import com.cumulus.entity.LocalStorage;
import com.cumulus.exception.BadRequestException;
import com.cumulus.service.LocalStorageService;
import com.cumulus.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 本地存储控制层
 *
 * @author zhaoff
 */
@RestController
@RequestMapping("/api/localStorage")
public class LocalStorageController {

    /**
     * 本地存储服务接口
     */
    @Autowired
    private LocalStorageService localStorageService;

    /**
     * 查询本地存储
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return 本地存储列表
     */
    @GetMapping
    @PreAuthorize("@auth.check('storage:list')")
    public ResponseEntity<Object> query(LocalStorageQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(localStorageService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    /**
     * 导出本地存储列表
     *
     * @param response 响应
     * @param criteria 查询参数
     * @throws IOException 异常
     */
    @GetMapping(value = "/download")
    @PreAuthorize("@auth.check('storage:list')")
    public void download(HttpServletResponse response, LocalStorageQueryCriteria criteria) throws IOException {
        localStorageService.download(localStorageService.queryAll(criteria), response);
    }

    /**
     * 上传文件
     *
     * @param name 文件名
     * @param file 文件
     * @return 响应
     */
    @PostMapping
    @PreAuthorize("@auth.check('storage:add')")
    public ResponseEntity<Object> create(@RequestParam String name, @RequestParam("file") MultipartFile file) {
        localStorageService.create(name, file);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 上传图片
     *
     * @param file 文件
     * @return 本地存储对象
     */
    @PostMapping("/pictures")
    public ResponseEntity<Object> upload(@RequestParam MultipartFile file) {
        // 判断文件是否为图片
        String suffix = FileUtils.getExtensionName(file.getOriginalFilename());
        if (!FileUtils.IMAGE.equals(FileUtils.getFileType(suffix))) {
            throw new BadRequestException("只能上传图片");
        }
        LocalStorage localStorage = localStorageService.create(null, file);
        return new ResponseEntity<>(localStorage, HttpStatus.OK);
    }

    /**
     * 修改本地存储
     *
     * @param resources 本地存储
     * @return 响应
     */
    @Log("修改文件")
    @PutMapping
    @PreAuthorize("@auth.check('storage:edit')")
    public ResponseEntity<Object> update(@Validated @RequestBody LocalStorage resources) {
        localStorageService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 批量删除本地存储
     *
     * @param ids 本地存储ID数组
     * @return 响应
     */
    @Log("删除文件")
    @DeleteMapping
    public ResponseEntity<Object> delete(@RequestBody Long[] ids) {
        localStorageService.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
