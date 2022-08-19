package com.cumulus.modules.mnt.controller;

import com.cumulus.annotation.Log;
import com.cumulus.modules.mnt.dto.DeployQueryCriteria;
import com.cumulus.modules.mnt.entity.Deploy;
import com.cumulus.modules.mnt.entity.DeployHistory;
import com.cumulus.modules.mnt.service.DeployService;
import com.cumulus.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 部署控制层
 */
@Slf4j
@RestController
@RequestMapping("/api/deploy")
public class DeployController {

    /**
     * 临时文件存放路径
     */
    private final String fileSavePath = FileUtils.getTmpDirPath() + "/";

    /**
     * 部署服务接口
     */
    @Autowired
    private DeployService deployService;

    /**
     * 导出部署数据
     *
     * @param response 响应
     * @param criteria 查询参数
     * @throws IOException 异常
     */
    @GetMapping(value = "/download")
    @PreAuthorize("@auth.check('database:list')")
    public void download(HttpServletResponse response, DeployQueryCriteria criteria) throws IOException {
        deployService.download(deployService.queryAll(criteria), response);
    }

    /**
     * 查询部署
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return 部署历史列表
     */
    @GetMapping
    @PreAuthorize("@auth.check('deploy:list')")
    public ResponseEntity<Object> query(DeployQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(deployService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    /**
     * 新增部署
     *
     * @param resources 部署
     * @return 响应
     */
    @Log("新增部署")
    @PostMapping
    @PreAuthorize("@auth.check('deploy:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody Deploy resources) {
        deployService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 修改部署
     *
     * @param resources 部署
     * @return 响应
     */
    @Log("修改部署")
    @PutMapping
    @PreAuthorize("@auth.check('deploy:edit')")
    public ResponseEntity<Object> update(@Validated @RequestBody Deploy resources) {
        deployService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 删除部署
     *
     * @param ids 部署ID集合
     * @return 响应
     */
    @Log("删除部署")
    @DeleteMapping
    @PreAuthorize("@auth.check('deploy:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        deployService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 上传文件部署
     *
     * @param file    部署文件
     * @param request 请求
     * @return 结果
     * @throws Exception 异常
     */
    @Log("上传文件部署")
    @PostMapping(value = "/upload")
    @PreAuthorize("@auth.check('deploy:edit')")
    public ResponseEntity<Object> upload(@RequestBody MultipartFile file, HttpServletRequest request) throws Exception {
        Long id = Long.valueOf(request.getParameter("id"));
        String fileName = "";
        if (null != file) {
            fileName = file.getOriginalFilename();
            File deployFile = new File(fileSavePath + fileName);
            FileUtils.del(deployFile);
            file.transferTo(deployFile);
            // 文件下一步要根据文件名字来
            deployService.deploy(fileSavePath + fileName, id);
        } else {
            if (log.isWarnEnabled()) {
                log.warn("The deploy file was not found");
            }
        }
        if (log.isInfoEnabled()) {
            log.info("The original name of the file upload is: " + Objects.requireNonNull(file).getOriginalFilename());
        }
        Map<String, Object> map = new HashMap<>(2);
        map.put("errno", 0);
        map.put("id", fileName);
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    /**
     * 系统还原
     *
     * @param resources 部署历史
     * @return 结果
     */
    @Log("系统还原")
    @PostMapping(value = "/serverReduction")
    @PreAuthorize("@auth.check('deploy:edit')")
    public ResponseEntity<Object> serverReduction(@Validated @RequestBody DeployHistory resources) {
        String result = deployService.serverReduction(resources);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * 服务运行状态
     *
     * @param resources 部署
     * @return 结果
     */
    @Log("服务运行状态")
    @PostMapping(value = "/serverStatus")
    @PreAuthorize("@auth.check('deploy:edit')")
    public ResponseEntity<Object> serverStatus(@Validated @RequestBody Deploy resources) {
        String result = deployService.serverStatus(resources);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * 启动服务
     *
     * @param resources 部署
     * @return 结果
     */
    @Log("启动服务")
    @PostMapping(value = "/startServer")
    @PreAuthorize("@auth.check('deploy:edit')")
    public ResponseEntity<Object> startServer(@Validated @RequestBody Deploy resources) {
        String result = deployService.startServer(resources);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * 停止服务
     *
     * @param resources 部署
     * @return 结果
     */
    @Log("停止服务")
    @PostMapping(value = "/stopServer")
    @PreAuthorize("@auth.check('deploy:edit')")
    public ResponseEntity<Object> stopServer(@Validated @RequestBody Deploy resources) {
        String result = deployService.stopServer(resources);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
