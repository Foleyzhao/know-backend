package com.cumulus.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据权限枚举
 */
@Getter
@AllArgsConstructor
public enum DataScopeEnum {

    /**
     * 全部的数据权限
     */
    ALL("全部", "全部的数据权限"),

    /**
     * 自己部门的数据权限
     */
    THIS_LEVEL("本级", "自己部门的数据权限"),

    /**
     * 自定义的数据权限
     */
    THIS_LEVEL_AND_BELOW("本级及以下", "本级及以下的数据权限");

    /**
     * 数据权限值
     */
    private final String value;

    /**
     * 数据权限秒速
     */
    private final String description;

    /**
     * 根据数据权限值获取枚举类型的数据权限
     *
     * @param val 数据权限值
     * @return 枚举类型的数据权限
     */
    public static DataScopeEnum find(String val) {
        for (DataScopeEnum dataScopeEnum : DataScopeEnum.values()) {
            if (val.equals(dataScopeEnum.getValue())) {
                return dataScopeEnum;
            }
        }
        return null;
    }

}
