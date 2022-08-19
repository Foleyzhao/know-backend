package com.cumulus.controller;

import com.cumulus.dto.LogQueryCriteria;
import com.cumulus.enums.LogTypeEnum;
import com.cumulus.mapstruct.LogErrorMapper;
import com.cumulus.mapstruct.SimpLogMapper;
import com.cumulus.service.LogService;
import com.cumulus.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 操作日志控制层
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/logs")
@PreAuthorize("@auth.check('logAudit')")
public class LogController {

    /**
     * 操作日志服务接口
     */
    private final LogService logService;

    /**
     * LogErrorDto 转换Mapper
     */
    private final LogErrorMapper logErrorMapper;

    /**
     * simpLogDto 转换Mapper
     */
    private final SimpLogMapper simpLogMapper;

    /**
     * 导出INFO类型的日志
     *
     * @param response 响应
     * @param criteria 查询参数
     * @throws IOException 异常
     */
    @GetMapping(value = "/download")
    public void download(HttpServletResponse response, LogQueryCriteria criteria) throws IOException {
        criteria.setLogTypeNe(LogTypeEnum.ERROR.getValue());
        logService.download(logService.queryAll(criteria), response);
    }

    /**
     * 导出ERROR类型的日志
     *
     * @param response 响应
     * @param criteria 查询参数
     * @throws IOException 异常
     */
    @GetMapping(value = "/error/download")
    public void downloadErrorLog(HttpServletResponse response, LogQueryCriteria criteria) throws IOException {
        criteria.setLogType(LogTypeEnum.ERROR.getValue());
        logService.download(logService.queryAll(criteria), response);
    }

    /**
     * 查询除ERROR之外全部的日志
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return INFO类型日志列表
     */
    @GetMapping()
    public ResponseEntity<Object> query(LogQueryCriteria criteria, Pageable pageable) {
        criteria.setLogTypeNe(LogTypeEnum.ERROR.getValue());
        return new ResponseEntity<>(logService.queryAll(criteria, pageable).map(simpLogMapper::toDto), HttpStatus.OK);
    }

    /**
     * 用户日志查询
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return 用户日志列表
     */
    @GetMapping(value = "/user")
    public ResponseEntity<Object> queryUserLog(LogQueryCriteria criteria, Pageable pageable) {
        criteria.setLogTypeNe(LogTypeEnum.ERROR.getValue());
        criteria.setBlurry(SecurityUtils.getCurrentUsername());
        return new ResponseEntity<>(logService.queryAllByUser(criteria, pageable), HttpStatus.OK);
    }

    /**
     * ERROR类型日志查询
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return ERROR类型日志列表
     */
    @GetMapping(value = "/error")
    public ResponseEntity<Object> queryErrorLog(LogQueryCriteria criteria, Pageable pageable) {
        criteria.setLogType(LogTypeEnum.ERROR.getValue());
        return new ResponseEntity<>(logService.queryAll(criteria, pageable).map(logErrorMapper::toDto), HttpStatus.OK);
    }

    /**
     * 日志异常详情查询
     *
     * @param id 操作日志ID
     * @return 日志异常详情
     */
    @GetMapping(value = "/error/{id}")
    public ResponseEntity<Object> queryErrorLogs(@PathVariable Long id) {
        return new ResponseEntity<>(logService.findErrDetailById(id), HttpStatus.OK);
    }

    /**
     * 删除所有ERROR类型的日志
     *
     * @return 响应
     */
    @DeleteMapping(value = "/del/error")
    public ResponseEntity<Object> delAllErrorLog() {
        logService.delAllErrorLog();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 删除所有INFO类型的日志
     *
     * @return 响应
     */
    @DeleteMapping(value = "/del/info")
    public ResponseEntity<Object> delAllInfoLog() {
        logService.delAllInfoLog();
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
