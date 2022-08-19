package com.cumulus.modules.mnt.controller;

import com.cumulus.annotation.Log;
import com.cumulus.modules.mnt.dto.AppQueryCriteria;
import com.cumulus.modules.mnt.entity.App;
import com.cumulus.modules.mnt.service.AppService;
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
 * 应用控制层
 */
@RestController
@RequestMapping("/api/app")
public class AppController {

    /**
     * 应用服务接口
     */
    @Autowired
    private AppService appService;

    /**
     * 导出应用数据
     *
     * @param response 响应
     * @param criteria 查询参数
     * @throws IOException 异常
     */
    @GetMapping(value = "/download")
    @PreAuthorize("@auth.check('app:list')")
    public void download(HttpServletResponse response, AppQueryCriteria criteria) throws IOException {
        appService.download(appService.queryAll(criteria), response);
    }

    /**
     * 查询应用
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return 应用列表
     */
    @GetMapping
    @PreAuthorize("@auth.check('app:list')")
    public ResponseEntity<Object> query(AppQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(appService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    /**
     * 新增应用
     *
     * @param resources 应用
     * @return 响应
     */
    @Log("新增应用")
    @PostMapping
    @PreAuthorize("@auth.check('app:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody App resources) {
        appService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 修改应用
     *
     * @param resources 应用
     * @return 响应
     */
    @Log("修改应用")
    @PutMapping
    @PreAuthorize("@auth.check('app:edit')")
    public ResponseEntity<Object> update(@Validated @RequestBody App resources) {
        appService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 删除应用
     *
     * @param ids 应用ID集合
     * @return 响应
     */
    @Log("删除应用")
    @DeleteMapping
    @PreAuthorize("@auth.check('app:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        appService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
