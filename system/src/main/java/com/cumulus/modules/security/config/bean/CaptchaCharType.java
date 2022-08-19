package com.cumulus.modules.security.config.bean;

import com.wf.captcha.base.Captcha;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 验证码字符 只有Spec 和Gif类型有效
 *
 * @author : shenjc
 */
@Getter
@AllArgsConstructor
public enum CaptchaCharType {

    /**
     *  验证码字符 只有Spec 和Gif类型有效
     */
    TYPE_DEFAULT(Captcha.TYPE_DEFAULT),
    TYPE_ONLY_NUMBER(Captcha.TYPE_ONLY_NUMBER),
    TYPE_ONLY_CHAR(Captcha.TYPE_ONLY_CHAR),
    TYPE_ONLY_UPPER(Captcha.TYPE_ONLY_UPPER),
    TYPE_ONLY_LOWER(Captcha.TYPE_ONLY_LOWER),
    TYPE_NUM_AND_UPPER(Captcha.TYPE_NUM_AND_UPPER);

    private final int value;
}
