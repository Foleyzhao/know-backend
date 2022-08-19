package com.cumulus.modules.quartz.utils;

import com.cumulus.utils.SpringContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * 定时任务执行实体
 *
 * @author zhaoff
 */
@Slf4j
public class QuartzRunnable implements Callable<Object> {

    /**
     * 目标Bean名称
     */
    private final Object target;

    /**
     * 方法名称
     */
    private final Method method;

    /**
     * 参数
     */
    private final Long params;

    /**
     * 构造方法
     *
     * @param beanName   Bean名称
     * @param methodName 方法名称
     * @param params     参数
     * @throws NoSuchMethodException 不存在方法异常
     * @throws SecurityException     认证异常
     */
    public QuartzRunnable(String beanName, String methodName, Long params)
            throws NoSuchMethodException, SecurityException {
        this.target = SpringContextHolder.getBean(beanName);
        this.params = params;
        if (null != params) {
            this.method = target.getClass().getDeclaredMethod(methodName, Long.class);
        } else {
            this.method = target.getClass().getDeclaredMethod(methodName);
        }
    }

    @Override
    public Object call() throws Exception {
        ReflectionUtils.makeAccessible(method);
        if (null != params) {
            method.invoke(target, params);
        } else {
            method.invoke(target);
        }
        return null;
    }

}
