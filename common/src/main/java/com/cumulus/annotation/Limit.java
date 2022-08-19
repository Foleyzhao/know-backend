package com.cumulus.annotation;

import com.cumulus.aspect.LimitType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义限流注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Limit {

    /**
     * 资源名称（用于接口功能描述）
     *
     * @return 资源名称
     */
    String name() default "";

    /**
     * 限流资源Redis key
     *
     * @return Redis key
     */
    String key() default "";

    /**
     * 限流资源Redis key前缀
     *
     * @return Redis key前缀
     */
    String prefix() default "";

    /**
     * 限流周期（单位秒）
     *
     * @return 限流周期
     */
    int period();

    /**
     * 限制访问次数
     *
     * @return 限制访问次数
     */
    int count();

    /**
     * 限流类型
     *
     * @return 限流类型
     */
    LimitType limitType() default LimitType.CUSTOMER;

}
