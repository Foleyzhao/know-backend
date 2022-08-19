package com.cumulus.annotation;

import com.cumulus.config.SizeChineseValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 中文字符长度校验注解 中文视为2个长度
 *
 * @author : shenjc
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SizeChineseValidator.class)
@Documented
public @interface SizeChinese {

    /**
     * 出错的返回消息
     */
    String message() default "";

    /**
     * 校验分组
     */
    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 最小长度
     */
    int min() default 0;

    /**
     * 最大长度
     */
    int max() default 2147483647;
}
