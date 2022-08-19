package com.cumulus.utils;

import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 分页工具类
 */
public class PageUtils extends cn.hutool.core.util.PageUtil {

    /**
     * 内存中进行分页
     *
     * @param page 当前页
     * @param size 分页大小
     * @param list 待分页数据
     * @return 分页数据
     */
    public static List<?> toPage(int page, int size, List<?> list) {
        int fromIndex = page * size;
        int toIndex = page * size + size;
        if (fromIndex > list.size()) {
            return new ArrayList<>();
        } else if (toIndex >= list.size()) {
            return list.subList(fromIndex, list.size());
        } else {
            return list.subList(fromIndex, toIndex);
        }
    }

    /**
     * 对分页结果重新封装
     *
     * @param page 分页结果
     * @return 自定义分页结果
     */
    public static Map<String, Object> toPage(Page<?> page) {
        Map<String, Object> map = new LinkedHashMap<>(2);
        map.put("content", page.getContent());
        map.put("totalElements", page.getTotalElements());
        return map;
    }

    /**
     * 对分页结果重新封装
     *
     * @param object        分页结果数据
     * @param totalElements 数据梳理
     * @return 自定义分页结果
     */
    public static Map<String, Object> toPage(Object object, Object totalElements) {
        Map<String, Object> map = new LinkedHashMap<>(2);
        map.put("content", object);
        map.put("totalElements", totalElements);
        return map;
    }

}
