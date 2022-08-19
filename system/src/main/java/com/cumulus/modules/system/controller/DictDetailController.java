package com.cumulus.modules.system.controller;

import com.cumulus.annotation.Log;
import com.cumulus.exception.BadRequestException;
import com.cumulus.modules.system.dto.DictDetailDto;
import com.cumulus.modules.system.dto.DictDetailQueryCriteria;
import com.cumulus.modules.system.entity.DictDetail;
import com.cumulus.modules.system.service.DictDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 字典详情控制层
 */
@RestController
@RequestMapping("/api/dictDetail")
public class DictDetailController {

    /**
     * 字典详情服务接口
     */
    @Autowired
    private DictDetailService dictDetailService;

    /**
     * 查询字典详情
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return 字典详情列表
     */
    @GetMapping
    public ResponseEntity<Object> query(DictDetailQueryCriteria criteria,
                                        @PageableDefault(sort = {"dictSort"}, direction = Sort.Direction.ASC)
                                                Pageable pageable) {
        return new ResponseEntity<>(dictDetailService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    /**
     * 根据字典名称查询字典详情
     *
     * @param dictName 字典名称
     * @return 字典详情列表与字典名称的映射
     */
    @GetMapping(value = "/map")
    public ResponseEntity<Object> getDictDetailMaps(@RequestParam String dictName) {
        String[] names = dictName.split("[,，]");
        Map<String, List<DictDetailDto>> dictMap = new HashMap<>(16);
        for (String name : names) {
            dictMap.put(name, dictDetailService.getDictByName(name));
        }
        return new ResponseEntity<>(dictMap, HttpStatus.OK);
    }

    /**
     * 新增字典详情
     *
     * @param resources 字典详情
     * @return 响应
     */
    @Log("新增字典详情")
    @PostMapping
    @PreAuthorize("@auth.check('dict:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody DictDetail resources) {
        if (null != resources.getId()) {
            throw new BadRequestException("A new dictDetail cannot already have an ID");
        }
        dictDetailService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 修改字典详情
     *
     * @param resources 字典详情
     * @return 响应
     */
    @Log("修改字典详情")
    @PutMapping
    @PreAuthorize("@auth.check('dict:edit')")
    public ResponseEntity<Object> update(@Validated(DictDetail.Update.class) @RequestBody DictDetail resources) {
        dictDetailService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 删除字典详情
     *
     * @param id 字典详情ID
     * @return 响应
     */
    @Log("删除字典详情")
    @DeleteMapping(value = "/{id}")
    @PreAuthorize("@auth.check('dict:del')")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        dictDetailService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
