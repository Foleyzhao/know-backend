package com.cumulus.modules.mnt.controller;

import com.cumulus.annotation.Log;
import com.cumulus.exception.BadRequestException;
import com.cumulus.modules.mnt.dto.DatabaseDto;
import com.cumulus.modules.mnt.dto.DatabaseQueryCriteria;
import com.cumulus.modules.mnt.entity.Database;
import com.cumulus.modules.mnt.service.DatabaseService;
import com.cumulus.modules.mnt.util.SqlUtils;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * 数据库控制层
 */
@RestController
@RequestMapping("/api/database")
public class DatabaseController {

    /**
     * 临时文件存放路径
     */
    private final String fileSavePath = FileUtils.getTmpDirPath() + "/";

    /**
     * 数据库服务接口
     */
    @Autowired
    private DatabaseService databaseService;

    /**
     * 导出数据库数据
     *
     * @param response 响应
     * @param criteria 查询条件
     * @throws IOException 异常
     */
    @GetMapping(value = "/download")
    @PreAuthorize("@auth.check('database:list')")
    public void download(HttpServletResponse response, DatabaseQueryCriteria criteria) throws IOException {
        databaseService.download(databaseService.queryAll(criteria), response);
    }

    /**
     * 查询数据库
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return 数据库列表
     */
    @GetMapping
    @PreAuthorize("@auth.check('database:list')")
    public ResponseEntity<Object> query(DatabaseQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(databaseService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    /**
     * 新增数据库
     *
     * @param resources 数据库
     * @return 响应
     */
    @Log("新增数据库")
    @PostMapping
    @PreAuthorize("@auth.check('database:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody Database resources) {
        databaseService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 修改数据库
     *
     * @param resources 数据库
     * @return 响应
     */
    @Log("修改数据库")
    @PutMapping
    @PreAuthorize("@auth.check('database:edit')")
    public ResponseEntity<Object> update(@Validated @RequestBody Database resources) {
        databaseService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 删除数据库
     *
     * @param ids 数据库ID集合
     * @return 响应
     */
    @Log("删除数据库")
    @DeleteMapping
    @PreAuthorize("@auth.check('database:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<String> ids) {
        databaseService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 测试数据库链接
     *
     * @param resources 数据库
     * @return 结果
     */
    @Log("测试数据库链接")
    @PostMapping("/testConnect")
    @PreAuthorize("@auth.check('database:testConnect')")
    public ResponseEntity<Object> testConnect(@Validated @RequestBody Database resources) {
        return new ResponseEntity<>(databaseService.testConnection(resources), HttpStatus.CREATED);
    }

    /**
     * 执行SQL脚本
     *
     * @param file    数据库脚本
     * @param request 请求
     * @return 结果
     * @throws Exception 异常
     */
    @Log("执行SQL脚本")
    @PostMapping(value = "/upload")
    @PreAuthorize("@auth.check('database:add')")
    public ResponseEntity<Object> upload(@RequestBody MultipartFile file, HttpServletRequest request) throws Exception {
        String id = request.getParameter("id");
        DatabaseDto database = databaseService.findById(id);
        String fileName;
        if (null != database) {
            fileName = file.getOriginalFilename();
            File executeFile = new File(fileSavePath + fileName);
            FileUtils.del(executeFile);
            file.transferTo(executeFile);
            String result = SqlUtils.executeFile(database.getJdbcUrl(), database.getUserName(), database.getPwd(),
                    executeFile);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            throw new BadRequestException("数据库不存在");
        }
    }
}
