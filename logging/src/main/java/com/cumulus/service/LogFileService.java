package com.cumulus.service;

import com.cumulus.dto.LogFileDTO;
import com.cumulus.dto.LogQueryCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 日志归档文件表 接口
 *
 * @author : shenjc
 */
public interface LogFileService {

    /**
     * 分页查询操作日志
     *
     * @param pageable 分页参数
     * @return 操作日志列表
     */
    Page<LogFileDTO> queryAll(Pageable pageable);

    /**
     * 生成日志归档文件
     */
    void saveLogFileAuto();

    /**
     * 生成日志归档文件
     *
     * @param criteria 查询条件
     */
    void saveLogFile(LogQueryCriteria criteria);
}
