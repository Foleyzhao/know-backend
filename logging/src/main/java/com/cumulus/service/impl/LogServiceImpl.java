package com.cumulus.service.impl;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.cumulus.entity.Log;
import com.cumulus.enums.LogTypeEnum;
import com.cumulus.repository.LogRepository;
import com.cumulus.service.LogService;
import com.cumulus.dto.LogQueryCriteria;
import com.cumulus.mapstruct.SimpLogMapper;
import com.cumulus.utils.DateUtils;
import com.cumulus.utils.FileUtils;
import com.cumulus.utils.PageUtils;
import com.cumulus.utils.QueryUtils;
import com.cumulus.utils.StringUtils;
import com.cumulus.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 操作日志服务实现
 */
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    /**
     * 操作日志数据访问接口
     */
    private final LogRepository logRepository;

    /**
     * 精简操作日志传输对象与操作日志实体的映射
     */
    private final SimpLogMapper simpLogMapper;

    @Override
    public Page<Log> queryAll(LogQueryCriteria criteria, Pageable pageable) {
        return logRepository.findAll(
                ((root, criteriaQuery, cb) -> QueryUtils.getPredicate(root, criteria, cb)), pageable);
    }

    @Override
    public List<Log> queryAll(LogQueryCriteria criteria) {
        return logRepository.findAll(((root, criteriaQuery, cb) -> QueryUtils.getPredicate(root, criteria, cb)));
    }

    @Override
    public Object queryAllByUser(LogQueryCriteria criteria, Pageable pageable) {
        Page<Log> page = logRepository.findAll(
                ((root, criteriaQuery, cb) -> QueryUtils.getPredicate(root, criteria, cb)), pageable);
        return PageUtils.toPage(page.map(simpLogMapper::toDto));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(String username, String browser, String ip, ProceedingJoinPoint joinPoint, Log log) {
        if (null == log) {
            throw new IllegalArgumentException("操作日志不能为空！");
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        com.cumulus.annotation.Log aopLog = method.getAnnotation(com.cumulus.annotation.Log.class);
        // 方法路径
        String methodName = joinPoint.getTarget().getClass().getName() + "." + signature.getName() + "()";
        // 描述
        log.setDescription(aopLog.value());
        log.setRequestIp(ip);
        log.setAddress(StringUtils.getCityInfo(log.getRequestIp()));
        log.setMethod(methodName);
        log.setUsername(username);
        log.setParams(getParameter(method, joinPoint.getArgs()));
        log.setBrowser(browser);
        if (!LogTypeEnum.ERROR.getValue().equals(log.getLogType())) {
            log.setLogType(aopLog.logType().getValue());
        }
        logRepository.save(log);
    }

    /**
     * 根据请求方法和传入的参数获取请求参数
     *
     * @param method 请求方法
     * @param args   传入的参数
     * @return 请求参数
     */
    private String getParameter(Method method, Object[] args) {
        List<Object> argList = new ArrayList<>();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            // 将RequestBody注解修饰的参数作为请求参数
            RequestBody requestBody = parameters[i].getAnnotation(RequestBody.class);
            if (null != requestBody) {
                argList.add(args[i]);
            }
            // 将RequestParam注解修饰的参数作为请求参数
            RequestParam requestParam = parameters[i].getAnnotation(RequestParam.class);
            if (null != requestParam) {
                Map<String, Object> map = new HashMap<>();
                String key = parameters[i].getName();
                if (!StringUtils.isEmpty(requestParam.value())) {
                    key = requestParam.value();
                }
                map.put(key, args[i]);
                argList.add(map);
            }
        }
        if (argList.isEmpty()) {
            return "";
        }
        return argList.size() == 1 ? JSONUtil.toJsonStr(argList.get(0)) : JSONUtil.toJsonStr(argList);
    }

    @Override
    public Object findErrDetailById(Long id) {
        Log log = logRepository.findById(id).orElseGet(Log::new);
        ValidationUtils.isNull(log.getId(), "Log", "id", id);
        byte[] details = log.getExceptionDetail();
        return Dict.create().set("exception", new String(ObjectUtil.isNotNull(details) ? details : "".getBytes()));
    }

    @Override
    public void download(List<Log> logs, HttpServletResponse response) throws IOException {
        FileUtils.downloadExcel(logToMapList(logs), response,
                "日志批量导出" + DateUtils.DFY_HMS.format(LocalDateTime.now()));
    }

    @Override
    public List<Map<String, Object>> logToMapList(List<Log> logs) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Log log : logs) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("用户名", log.getUsername());
            map.put("IP", log.getRequestIp());
            map.put("IP来源", log.getAddress());
            map.put("描述", log.getDescription());
            map.put("浏览器", log.getBrowser());
            map.put("请求耗时/毫秒", log.getTime());
            map.put("异常详情", new String(ObjectUtil.isNotNull(log.getExceptionDetail()) ?
                    log.getExceptionDetail() : "".getBytes()));
            map.put("创建日期", log.getCreateTime());
            list.add(map);
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delAllErrorLog() {
        logRepository.deleteByLogType(LogTypeEnum.ERROR.getValue());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delAllInfoLog() {
        logRepository.deleteByLogType(LogTypeEnum.INFO.getValue());
    }
}
