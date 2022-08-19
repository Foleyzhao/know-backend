package com.cumulus.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring应用上下文工具类
 */
@Slf4j
public class SpringContextHolder implements ApplicationContextAware, DisposableBean {

    /**
     * Spring应用上下文
     */
    private static ApplicationContext applicationContext = null;

    /**
     * 回调函数列表
     */
    private static final List<CallBack> CALL_BACKS = new ArrayList<>();

    /**
     * 是否允许增加回调函数
     */
    private static boolean addCallback = true;

    /**
     * 新增回调函数
     *
     * @param callBack 回调函数
     */
    public synchronized static void addCallBacks(CallBack callBack) {
        if (addCallback) {
            SpringContextHolder.CALL_BACKS.add(callBack);
        } else {
            if (log.isWarnEnabled()) {
                log.warn("CallBack: {} Couldn't add! Execute immediately.", callBack.getCallBackName());
            }
            callBack.executor();
        }
    }

    /**
     * 从Spring应用上下文中获取Bean
     *
     * @param name Bean名称
     * @param <T>  Bean类型
     * @return Bean
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        assertContextInjected();
        return (T) applicationContext.getBean(name);
    }

    /**
     * 从Spring应用上下文中获取Bean
     *
     * @param requiredType Bean类型
     * @param <T>          Bean类型
     * @return Bean
     */
    public static <T> T getBean(Class<T> requiredType) {
        assertContextInjected();
        return applicationContext.getBean(requiredType);
    }

    /**
     * 获取SpringBoot配置信息
     *
     * @param property     配置属性键
     * @param defaultValue 配置属性默认值
     * @param requiredType 配置属性值类型
     * @return 配置属性值
     */
    public static <T> T getProperties(String property, T defaultValue, Class<T> requiredType) {
        T result = defaultValue;
        try {
            result = getBean(Environment.class).getProperty(property, requiredType);
        } catch (Exception ignored) {
        }
        return result;
    }

    /**
     * 获取SpringBoot配置信息
     *
     * @param property 配置属性键
     * @return 配置属性值
     */
    public static String getProperties(String property) {
        return getProperties(property, null, String.class);
    }

    /**
     * 获取SpringBoot配置信息
     *
     * @param property     配置属性键
     * @param requiredType 配置属性值类型
     * @return 配置属性值
     */
    public static <T> T getProperties(String property, Class<T> requiredType) {
        return getProperties(property, null, requiredType);
    }

    /**
     * 检查Spring应用上下文不为空
     */
    private static void assertContextInjected() {
        if (null == applicationContext) {
            throw new IllegalStateException("applicationContext属性未注入, 请在applicationContext.xml中定义" +
                    "SpringContextHolder或在SpringBoot启动类中注册SpringContextHolder.");
        }
    }

    /**
     * 清除Spring应用上下文为Null
     */
    private static void clearHolder() {
        if (log.isDebugEnabled()) {
            log.debug("清除SpringContextHolder中的ApplicationContext:" + applicationContext);
        }
        applicationContext = null;
    }

    @Override
    public void destroy() {
        SpringContextHolder.clearHolder();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (null != SpringContextHolder.applicationContext) {
            if (log.isWarnEnabled()) {
                log.warn("SpringContextHolder中的ApplicationContext被覆盖, 原有ApplicationContext为:"
                        + SpringContextHolder.applicationContext);
            }
        }
        SpringContextHolder.applicationContext = applicationContext;
        if (addCallback) {
            for (CallBack callBack : SpringContextHolder.CALL_BACKS) {
                callBack.executor();
            }
            CALL_BACKS.clear();
        }
        SpringContextHolder.addCallback = false;
    }

}
