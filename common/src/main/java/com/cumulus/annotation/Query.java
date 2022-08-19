package com.cumulus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义查询注解
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {

    /**
     * 基本对象的属性名
     */
    String propName() default "";

    /**
     * 查询操作
     */
    Type type() default Type.EQUAL;

    /**
     * 连接查询的属性名
     */
    String joinName() default "";

    /**
     * 连接查询方式
     */
    Join join() default Join.LEFT;

    /**
     * 多字段模糊搜索
     */
    String blurry() default "";

    /**
     * 查询操作枚举
     */
    enum Type {
        // 相等
        EQUAL,
        // 大于或等于
        GREATER_THAN,
        // 小于或等于
        LESS_THAN,
        // 中模糊查询
        INNER_LIKE,
        // 左模糊查询
        LEFT_LIKE,
        // 右模糊查询
        RIGHT_LIKE,
        // 小于
        LESS_THAN_NQ,
        // 包含
        IN,
        // 不包含
        NOT_IN,
        // 不等于
        NOT_EQUAL,
        // 在......之间
        BETWEEN,
        // 不为空
        NOT_NULL,
        // 为空
        IS_NULL
    }

    /**
     * 连接查询类型枚举
     */
    enum Join {
        // 左连接
        LEFT,
        // 右连接
        RIGHT,
        // 内连接
        INNER
    }

}

