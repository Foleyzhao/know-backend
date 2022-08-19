package com.cumulus.controller;

import com.cumulus.dto.LogQueryCriteria;
import com.cumulus.entity.LogFile;
import com.cumulus.exception.BadRequestException;
import com.cumulus.repository.LogFileRepository;
import com.cumulus.service.LogFileService;
import com.cumulus.utils.FileUtils;
import com.cumulus.utils.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Optional;

/**
 * 日志归档类控制层
 *
 * @author : shenjc
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/logFile")
@PreAuthorize("@auth.check('logAudit')")
public class LogFileController {

    @Autowired
    private LogFileService logFileService;

    @Autowired
    private LogFileRepository logFileRepository;

    /**
     * 归档日志查询 目前没有复杂查询
     *
     * @param pageable 分页参数
     * @return INFO类型日志列表
     */
    @GetMapping
    public ResponseEntity<Object> fileQuery(Pageable pageable) {
        return new ResponseEntity<>(PageUtils.toPage(logFileService.queryAll(pageable)), HttpStatus.OK);
    }

    /**
     * 日志归档 目前没有复杂查询
     *
     * @return INFO类型日志列表
     */
    @GetMapping("saveLogFile")
    public ResponseEntity<Object> saveLogFile(LogQueryCriteria criteria) {
        logFileService.saveLogFile(criteria);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 下载日志归档xlsx
     *
     * @return INFO类型日志列表
     */
    @GetMapping("downloadLogFile")
    public ResponseEntity<Object> downloadLogFile(HttpServletRequest request, HttpServletResponse response, Long id) {
        Optional<LogFile> logFileOpt = logFileRepository.findById(id);
        if (logFileOpt.isPresent()) {
            FileUtils.downloadFile(request, response, new File(logFileOpt.get().getFilePath()));
            return new ResponseEntity<>(HttpStatus.OK);
        }
        throw new BadRequestException("文件不存在");
    }
}
