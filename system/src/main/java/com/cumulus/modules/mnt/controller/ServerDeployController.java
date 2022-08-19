package com.cumulus.modules.mnt.controller;

import com.cumulus.annotation.Log;
import com.cumulus.modules.mnt.dto.ServerDeployQueryCriteria;
import com.cumulus.modules.mnt.entity.ServerDeploy;
import com.cumulus.modules.mnt.service.ServerDeployService;
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

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * 服务器控制层
 */
@RestController
@RequestMapping("/api/serverDeploy")
public class ServerDeployController {

    /**
     * 服务器服务接口
     */
    @Autowired
    private ServerDeployService serverDeployService;

    /**
     * 导出服务器数据
     *
     * @param response 响应
     * @param criteria 查询参数
     * @throws IOException 异常
     */
    @GetMapping(value = "/download")
    @PreAuthorize("@auth.check('serverDeploy:list')")
    public void download(HttpServletResponse response, ServerDeployQueryCriteria criteria) throws IOException {
        serverDeployService.download(serverDeployService.queryAll(criteria), response);
    }

    /**
     * 查询服务器
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return 服务器列表
     */
    @GetMapping
    @PreAuthorize("@auth.check('serverDeploy:list')")
    public ResponseEntity<Object> query(ServerDeployQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(serverDeployService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    /**
     * 新增服务器
     *
     * @param resources 服务器
     * @return 响应
     */
    @Log("新增服务器")
    @PostMapping
    @PreAuthorize("@auth.check('serverDeploy:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody ServerDeploy resources) {
        serverDeployService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 修改服务器
     *
     * @param resources 服务器
     * @return 响应
     */
    @Log("修改服务器")
    @PutMapping
    @PreAuthorize("@auth.check('serverDeploy:edit')")
    public ResponseEntity<Object> update(@Validated @RequestBody ServerDeploy resources) {
        serverDeployService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 删除服务器
     *
     * @param ids 服务器ID集合
     * @return 响应
     */
    @Log("删除服务器")
    @DeleteMapping
    @PreAuthorize("@auth.check('serverDeploy:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        serverDeployService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 测试连接服务器
     *
     * @param resources 服务器
     * @return 测试结果
     */
    @Log("测试连接服务器")
    @PostMapping("/testConnect")
    @PreAuthorize("@auth.check('serverDeploy:add')")
    public ResponseEntity<Object> testConnect(@Validated @RequestBody ServerDeploy resources) {
        return new ResponseEntity<>(serverDeployService.testConnect(resources), HttpStatus.CREATED);
    }
}
