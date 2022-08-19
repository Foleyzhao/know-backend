package com.cumulus.exception;

import org.springframework.util.StringUtils;

/**
 * 自定义实体不存在异常
 */
public class EntityNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 4627687364969860698L;

    /**
     * 构造方法
     *
     * @param clazz 实体类
     * @param field 实体字段
     * @param val   字段值
     */
    public EntityNotFoundException(Class<?> clazz, String field, String val) {
        super(EntityNotFoundException.generateMessage(clazz.getSimpleName(), field, val));
    }

    /**
     * 生成异常信息
     *
     * @param entity 实体
     * @param field  实体字段
     * @param val    字段值
     * @return 异常信息
     */
    private static String generateMessage(String entity, String field, String val) {
        return StringUtils.capitalize(entity) + " with " + field + " " + val + " does not exist";
    }

}
