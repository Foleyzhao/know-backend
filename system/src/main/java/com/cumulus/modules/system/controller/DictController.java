package com.cumulus.modules.system.controller;

import com.cumulus.annotation.Log;
import com.cumulus.exception.BadRequestException;
import com.cumulus.modules.system.dto.DictQueryCriteria;
import com.cumulus.modules.system.entity.Dict;
import com.cumulus.modules.system.service.DictService;
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
 * 字典控制层
 */
@RestController
@RequestMapping("/api/dict")
public class DictController {

    /**
     * 系统字典服务接口
     */
    @Autowired
    private DictService dictService;

    /**
     * 导出字典数据
     *
     * @param response 响应
     * @param criteria 查询参数
     * @throws IOException 异常
     */
    @GetMapping(value = "/download")
    @PreAuthorize("@auth.check('dict:list')")
    public void download(HttpServletResponse response, DictQueryCriteria criteria) throws IOException {
        dictService.download(dictService.queryAll(criteria), response);
    }

    /**
     * 查询字典
     *
     * @return 字典列表
     */
    @GetMapping(value = "/all")
    @PreAuthorize("@auth.check('dict:list')")
    public ResponseEntity<Object> queryAll() {
        return new ResponseEntity<>(dictService.queryAll(new DictQueryCriteria()), HttpStatus.OK);
    }

    /**
     * 查询字典
     *
     * @param resources 查询参数
     * @param pageable  分页参数
     * @return 响应
     */
    @GetMapping
    @PreAuthorize("@auth.check('dict:list')")
    public ResponseEntity<Object> query(DictQueryCriteria resources, Pageable pageable) {
        return new ResponseEntity<>(dictService.queryAll(resources, pageable), HttpStatus.OK);
    }

    /**
     * 新增字典
     *
     * @param resources 字典
     * @return 响应
     */
    @Log("新增字典")
    @PostMapping
    @PreAuthorize("@auth.check('dict:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody Dict resources) {
        if (null != resources.getId()) {
            throw new BadRequestException("A new dict cannot already have an ID");
        }
        dictService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 修改字典
     *
     * @param resources 字典
     * @return 响应
     */
    @Log("修改字典")
    @PutMapping
    @PreAuthorize("@auth.check('dict:edit')")
    public ResponseEntity<Object> update(@Validated(Dict.Update.class) @RequestBody Dict resources) {
        dictService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 删除字典
     *
     * @param ids 字典ID集合
     * @return 响应
     */
    @Log("删除字典")
    @DeleteMapping
    @PreAuthorize("@auth.check('dict:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        dictService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
