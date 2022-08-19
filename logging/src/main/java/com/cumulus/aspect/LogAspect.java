package com.cumulus.aspect;

import com.cumulus.entity.Log;
import com.cumulus.enums.LogTypeEnum;
import com.cumulus.service.LogService;
import com.cumulus.utils.RequestHolder;
import com.cumulus.utils.SecurityUtils;
import com.cumulus.utils.StringUtils;
import com.cumulus.utils.ThrowableUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 操作日志切面实现
 */
@Slf4j
@Component
@Aspect
public class LogAspect {

    /**
     * 操作日志服务接口
     */
    private final LogService logService;

    /**
     * 当前系统时间
     */
    ThreadLocal<Long> currentTime = new ThreadLocal<>();

    /**
     * 构造方法
     *
     * @param logService 操作日志服务接口
     */
    public LogAspect(LogService logService) {
        this.logService = logService;
    }

    /**
     * 程序切点
     */
    @Pointcut("@annotation(com.cumulus.annotation.Log)")
    public void logPointcut() {

    }

    /**
     * 环绕通知操作
     *
     * @param joinPoint 程序切点
     */
    @Around("logPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result;
        currentTime.set(System.currentTimeMillis());
        result = joinPoint.proceed();
        Log log = new Log(LogTypeEnum.INFO.getValue(), System.currentTimeMillis() - currentTime.get());
        currentTime.remove();
        HttpServletRequest request = RequestHolder.getHttpServletRequest();
        logService.save(getUsername(), StringUtils.getBrowser(request), StringUtils.getIp(request), joinPoint, log);
        return result;
    }

    /**
     * 异常通知操作
     *
     * @param joinPoint 程序切点
     * @param e         异常
     */
    @AfterThrowing(pointcut = "logPointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        Log log = new Log(LogTypeEnum.ERROR.getValue(), System.currentTimeMillis() - currentTime.get());
        currentTime.remove();
        log.setExceptionDetail(ThrowableUtils.getStackTrace(e).getBytes());
        HttpServletRequest request = RequestHolder.getHttpServletRequest();
        logService.save(getUsername(), StringUtils.getBrowser(request), StringUtils.getIp(request),
                (ProceedingJoinPoint) joinPoint, log);
    }

    /**
     * 获取当前用户的用户名
     *
     * @return 当前用户的用户名
     */
    public String getUsername() {
        try {
            return SecurityUtils.getCurrentUsername();
        } catch (Exception e) {
            return "";
        }
    }

}
