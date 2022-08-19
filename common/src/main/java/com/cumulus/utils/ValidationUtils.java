package com.cumulus.utils;

import cn.hutool.core.util.ObjectUtil;
import com.cumulus.exception.BadRequestException;

import java.util.regex.Pattern;

/**
 * 校验工具类
 */
public class ValidationUtils {

    /**
     * 校验是否为空
     *
     * @param obj       校验对象
     * @param entity    校验实体
     * @param parameter 校验字段
     * @param value     校验值
     */
    public static void isNull(Object obj, String entity, String parameter, Object value) {
        if (ObjectUtil.isNull(obj)) {
            String msg = entity + " 不存在: " + parameter + " is " + value;
            throw new BadRequestException(msg);
        }
    }

    /**
     * 校验是否为邮箱
     *
     * @param email 邮箱
     * @return 是否为邮箱
     */
    public static boolean isEmail(String email) {
        if (null != email && !email.isEmpty()) {
            return Pattern.matches("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*", email);
        }
        return false;
    }

}
