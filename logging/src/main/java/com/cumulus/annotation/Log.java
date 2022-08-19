package com.cumulus.annotation;

import com.cumulus.enums.LogTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义操作日志注解
 *
 * @author shenjc
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {

    /**
     * 描述内容
     */
    String value() default "";

    /**
     * 日志类型
     */
    LogTypeEnum logType() default LogTypeEnum.INFO;
}
