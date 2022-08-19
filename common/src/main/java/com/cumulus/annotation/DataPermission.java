package com.cumulus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义数据权限（系统部门）注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataPermission {

    /**
     * 实体中的字段名称
     */
    String fieldName() default "";

    /**
     * 实体中与部门关联的字段名称
     */
    String joinName() default "";

}
