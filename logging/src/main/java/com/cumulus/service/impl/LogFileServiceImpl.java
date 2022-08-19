package com.cumulus.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cumulus.config.FileProperties;
import com.cumulus.constant.FileConstant;
import com.cumulus.dto.LogFileDTO;
import com.cumulus.dto.LogQueryCriteria;
import com.cumulus.entity.Log;
import com.cumulus.entity.LogFile;
import com.cumulus.exception.BadRequestException;
import com.cumulus.mapstruct.LogFileMapper;
import com.cumulus.repository.LogFileRepository;
import com.cumulus.service.LogFileService;
import com.cumulus.service.LogService;
import com.cumulus.utils.DateUtils;
import com.cumulus.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author : shenjc
 */
@Service
@Slf4j
public class LogFileServiceImpl implements LogFileService {

    /**
     * 日志归档文件对象数据层接口
     */
    @Autowired
    private LogFileRepository logFileRepository;

    /**
     * 日志对象服务
     */
    @Autowired
    private LogService logService;

    /**
     * 日志归档实体和 DTO的 mapper
     */
    @Autowired
    private LogFileMapper logFileMapper;

    /**
     * 系统文件属性配置
     */
    @Autowired
    private FileProperties fileProperties;

    @Override
    public Page<LogFileDTO> queryAll(Pageable pageable) {
        Page<LogFile> page = logFileRepository.findAll(pageable);
        return page.map(logFileMapper::toDto);
    }

    @Override
    public void saveLogFileAuto() {
        List<Log> logs = logService.queryAll(null);
        if (log.isInfoEnabled()) {
            log.info("start saveLogFileAuto");
        }
        createLogFile(logs, DateUtils.DFY_MD_HMS.format(LocalDateTime.now()), LogFile.FILE_TYPE_AUTO);
    }

    @Override
    public void saveLogFile(LogQueryCriteria criteria) {
        if (criteria.getCreateTime() == null || criteria.getCreateTime().size() != LogQueryCriteria.CREATE_TIME_SIZE) {
            throw new BadRequestException("手动归档必须规定起止时间");
        }
        List<Log> logs = logService.queryAll(criteria);
        if (log.isInfoEnabled()) {
            log.info("start saveLogFile between{}, logs size:{}", JSONObject.toJSONString(criteria.getCreateTime()), logs.size());
        }
        if (logs.isEmpty()) {
            throw new BadRequestException("没有相关日志无法生成");
        }
        createLogFile(logs,
                DateUtils.DFY_MD_HMS.format(criteria.getCreateTime().get(0).toLocalDateTime())
                        + LogFile.HANDLE_TYPE_SEPARATE
                        + DateUtils.DFY_MD_HMS.format(criteria.getCreateTime().get(1).toLocalDateTime()),
                LogFile.FILE_TYPE_HANDLE);
    }

    /**
     * 生成 LogFile记录
     *
     * @param logs        日志对象
     * @param archiveTime 日志的归档时间 存在 时间间隔和时间点
     * @param fileType    手动和自动归档
     */
    private void createLogFile(List<Log> logs, String archiveTime, Integer fileType) {
        if (logs.isEmpty()) {
            return;
        }
        String fileName = LogFile.LOG_FILE_PREFIX + System.currentTimeMillis() + FileConstant.EXCEL_SUFFIX_XLSX;
        String filePath = fileProperties.getPath().getPath() +
                FileConstant.FILE_SEPARATE +
                FileConstant.LOG_FILE_FOLDER +
                FileConstant.FILE_SEPARATE +
                fileName;
        filePath = FileUtils.saveExcelFile(logService.logToMapList(logs), filePath);
        LogFile logFile = new LogFile();
        logFile.setFilePath(filePath);
        logFile.setFileType(fileType);
        logFile.setArchiveTime(archiveTime);
        logFile.setFileName(fileName);
        logFileRepository.save(logFile);
    }
}
