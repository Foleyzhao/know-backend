package com.cumulus.service;

import com.cumulus.entity.Log;
import com.cumulus.dto.LogQueryCriteria;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 操作日志服务接口
 */
public interface LogService {

    /**
     * 分页查询操作日志
     *
     * @param criteria 查询条件
     * @param pageable 分页参数
     * @return 操作日志列表
     */
    Page<Log> queryAll(LogQueryCriteria criteria, Pageable pageable);

    /**
     * 查询全部操作日志
     *
     * @param criteria 查询条件
     * @return 操作日志列表
     */
    List<Log> queryAll(LogQueryCriteria criteria);

    /**
     * 查询操作日志（用户日志查询）
     *
     * @param criteria 查询条件
     * @param pageable 分页参数
     * @return 操作日志列表
     */
    Object queryAllByUser(LogQueryCriteria criteria, Pageable pageable);

    /**
     * 保存操作日志
     *
     * @param username  用户名
     * @param browser   浏览器
     * @param ip        请求IP
     * @param joinPoint 程序切点
     * @param log       操作日志实体
     */
    @Async
    void save(String username, String browser, String ip, ProceedingJoinPoint joinPoint, Log log);

    /**
     * 根据操作日志ID查询异常详情
     *
     * @param id 操作日志ID
     * @return 异常详情
     */
    Object findErrDetailById(Long id);

    /**
     * 导出操作日志
     *
     * @param logs     操作日志列表
     * @param response 响应
     * @throws IOException 异常
     */
    void download(List<Log> logs, HttpServletResponse response) throws IOException;

    /**
     * 删除所有ERROR类型的操作日志
     */
    void delAllErrorLog();

    /**
     * 删除所有INFO类型的操作日志
     */
    void delAllInfoLog();

    /**
     * 列表转变为 mapList 且map Key为中文
     *
     * @param logs 操作日志列表
     * @return 返回Map列表
     */
    List<Map<String, Object>> logToMapList(List<Log> logs);
}
