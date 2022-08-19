package com.cumulus.config;

import com.cumulus.annotation.SizeChinese;
import com.cumulus.utils.RegexUtil;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 中文字符长度校验实现类
 *
 * @author : shenjc
 */
public class SizeChineseValidator implements ConstraintValidator<SizeChinese, String> {
    private int min;
    private int max;

    @Override
    public void initialize(SizeChinese constraintAnnotation) {
        min = constraintAnnotation.min();
        max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null) {
            return true;
        }
        final int size = RegexUtil.hexLength(s);
        return size <= max && size >= min;
    }
}
