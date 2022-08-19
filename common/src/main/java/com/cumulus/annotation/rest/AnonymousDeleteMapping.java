package com.cumulus.annotation.rest;

import com.cumulus.annotation.AnonymousAccess;
import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义匿名访问注解（DELETE方法）
 */
@AnonymousAccess
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RequestMapping(method = RequestMethod.DELETE)
public @interface AnonymousDeleteMapping {

    /**
     * 映射的名称
     */
    @AliasFor(annotation = RequestMapping.class)
    String name() default "";

    /**
     * 请求路径的地址
     */
    @AliasFor(annotation = RequestMapping.class)
    String[] value() default {};

    /**
     * 请求路径的地址
     */
    @AliasFor(annotation = RequestMapping.class)
    String[] path() default {};

    /**
     * 参数的类型
     */
    @AliasFor(annotation = RequestMapping.class)
    String[] params() default {};

    /**
     * 请求头内容
     */
    @AliasFor(annotation = RequestMapping.class)
    String[] headers() default {};

    /**
     * 数据请求的格式
     */
    @AliasFor(annotation = RequestMapping.class)
    String[] consumes() default {};

    /**
     * 返回的内容类型
     */
    @AliasFor(annotation = RequestMapping.class)
    String[] produces() default {};

}
