package com.cumulus.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 扫描产品常量枚举
 *
 * @author shenjc
 */
@Getter
@AllArgsConstructor
public enum ScanProduct {

    /**
     * 扫描产品常量
     */
//    YUAN_VULNERABILITY("御安漏洞管理平台", 1, Collections.singletonList("默认版本")),
    NSFOCUS_RSAS("NSFOCUS RSAS", 2, Collections.singletonList("V6.0R04F00SP06"));

    /**
     * 名称
     */
    private final String name;

    /**
     * 类似主键
     */
    private final int id;

    /**
     * 版本
     */
    private final List<String> version;

    /**
     * 根据id 获取漏扫器名称
     */
    public static String getNameById(Integer id) {
        if (id == null) {
            return null;
        }
        for (ScanProduct value : ScanProduct.values()) {
            if (value.getId() == id) {
                return value.getName();
            }
        }
        return String.valueOf(id);
    }

    /**
     * 根据id 获取漏扫器信息
     */
    public static ScanProduct getById(Integer id) {
        if (id == null) {
            return null;
        }
        for (ScanProduct value : ScanProduct.values()) {
            if (value.getId() == id) {
                return value;
            }
        }
        return null;
    }

    public static List<Map<String, Object>> getScanTypeList() {
        List<Map<String, Object>> scanTypeList = new ArrayList<>();
        for (ScanProduct value : ScanProduct.values()) {
            Map<String, Object> scanType = new HashMap<>(2);
            scanType.put("name", value.getName());
            scanType.put("value", value.getId());
            scanTypeList.add(scanType);
        }
        return scanTypeList;
    }

    public static final int BUILTIN_PRODUCT_ID = 1;

    public static final String BUILTIN_PRODUCT_NAME = "内置引擎";
}