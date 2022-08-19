package com.cumulus.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公共模块
 *
 * @author zhaoff
 */
public class CommonUtils {


    /**
     * 常用分隔符
     */
    public static final String COMMON_INTERVAL_CHARACTER = ",";


    /**
     * 常用分隔符
     */
    public static final String HTML_TD = "<td>";
    public static final String HTML_BACKSLASH_TD = "</td>";
    public static final String HTML_TR = "<tr>";
    public static final String HTML_BACKSLASH_TR = "</tr>";
    public static final String HTML_TABLE_ROWS = "<td rowspan=\"%s\">";

    /**
     * 将 map 转为 List<Map> 统一前端下拉列表的格式为 [{"name":"xxx","value":"xxx"}...] name 为前端显示 ，value 为前端发给后端的值.
     *
     * @param map       原始数据
     * @param keyToName keyToName true 则使用 map 的 key 作为 返回值的 name 否则相反
     * @return 返回 [{"name":"xxx","value":"xxx"}...]格式的数据
     */
    public static List<Map<String, Object>> mapToList(Map<String, Object> map, boolean keyToName) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        if (map == null) {
            return mapList;
        }
        String nameString = "name";
        String valueString = "value";
        for (String key : map.keySet()) {
            Object value = map.get(key);
            Map<String, Object> item = new HashMap<>(2);
            if (keyToName) {
                item.put(nameString, key);
                item.put(valueString, value);
            } else {
                item.put(nameString, value);
                item.put(valueString, key);
            }
            mapList.add(item);
        }
        return mapList;
    }

    /**
     * 将 List<map> 转为 List<Map> 统一前端下拉列表的格式为 [{"name":"xxx","value":"xxx"}...] name 为前端显示 ，value 为前端发给后端的值.
     *
     * @param mapList  原始数据
     * @param nameKey  原始数据中单个 map 作为结果的 name 值的 key
     * @param valueKey 原始数据中单个 map 作为结果的 value 值的 key
     * @return 返回 [{"name":"xxx","value":"xxx"}...]格式的数据
     */
    public static List<Map<String, Object>> listMapToListMap(List<Map<String, Object>> mapList, String nameKey, String valueKey) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (mapList == null || StringUtils.isEmpty(nameKey) || StringUtils.isEmpty(valueKey)) {
            return result;
        }
        String nameString = "name";
        String valueString = "value";
        for (Map<String, Object> map : mapList) {
            Map<String, Object> item = new HashMap<>(2);
            item.put(nameString, map.get(nameKey));
            item.put(valueString, map.get(valueKey));
            result.add(item);
        }
        return result;
    }

    /**
     * 由于 @RequestBody 中不能转换 MultiValueMap 对象所以使用该方法将获得的 map 转换为 MultiValueMap
     *
     * @param map map 对象
     * @return MultiValueMap 对象
     */
    public static MultiValueMap<String, String> mapToMultiValueMap(Map<String, String> map) {
        LinkedMultiValueMap<String, String> linkedMultiValueMap = new LinkedMultiValueMap<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            linkedMultiValueMap.add(entry.getKey(), entry.getValue());
        }
        return linkedMultiValueMap;
    }

    /**
     * 格式化List为 String
     *
     * @param list              列表
     * @param intervalCharacter 间隔符号
     * @return 返回字符串
     */
    public static String getFormatList(List<String> list, String intervalCharacter) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String str : list) {
            stringBuilder.append(str).append(intervalCharacter);
        }
        if (stringBuilder.length() != 0) {
            stringBuilder.delete(stringBuilder.length() - intervalCharacter.length(), stringBuilder.length());
        }
        return stringBuilder.toString();
    }
}
