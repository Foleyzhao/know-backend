package com.cumulus.modules.business.gather.service.gather;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 输出结果的解析接口
 *
 * @author zhaoff
 */
public abstract class OutputParser {

    /**
     * el 解析器
     */
    protected ExpressionParser elParser = new SpelExpressionParser();

    /**
     * 获取解析器的名称
     *
     * @return 解析器的名称
     */
    public abstract String getName();

    /**
     * 使用解析后的结果，对表达式进行计算，获取 clz 类型的结果
     *
     * @param expression 表达式
     * @param variables  变量
     * @param clz        返回结果的类型
     * @param <E>        泛型
     * @return 解析后的结果
     */
    public <E> E evaluate(String expression, Map<String, Object> variables, Class<E> clz) {
        Expression exp = elParser.parseExpression(expression);
        StandardEvaluationContext ctx = new StandardEvaluationContext(new ParserRoot());
        ctx.setVariables(variables);
        return exp.getValue(ctx, clz);
    }

    /**
     * 转换工具
     *
     * @author zhaoff
     */
    static class ParserRoot {

        // --- 数学统计处理

        /**
         * 获取最大值
         *
         * @param data 数据
         * @return 最大值
         */
        public Object max(List<Object> data) {
            return max(data.toArray(new Object[0]));
        }

        /**
         * 取最大值
         *
         * @param data 数据
         * @return 最大值
         */
        @SuppressWarnings({"rawtypes", "unchecked"})
        public Object max(Object[] data) {
            Number[] numbers = toNumberArray(data);
            if (null != numbers) {
                Number max = null;
                for (Number num : numbers) {
                    if (null == max || max.doubleValue() < num.doubleValue()) {
                        max = num;
                    }
                }
                return max;
            } else {
                Comparable max = null;
                for (Object d : data) {
                    if (!(d instanceof Comparable)) {
                        throw new IllegalArgumentException("data is not comparable: " + Arrays.toString(data));
                    }
                    if (null == max || max.compareTo(d) < 0) {
                        max = (Comparable) d;
                    }
                }
                return max;
            }
        }

        /**
         * 取最小值
         *
         * @param data 数据
         * @return 最小值
         */
        public Object min(List<Object> data) {
            return min(data.toArray(new Object[0]));
        }

        /**
         * 取最小值
         *
         * @param data 数据
         * @return 最小值
         */
        @SuppressWarnings({"rawtypes", "unchecked"})
        public Object min(Object[] data) {
            Number[] numbers = toNumberArray(data);
            if (null != numbers) {
                Number min = null;
                for (Number num : numbers) {
                    if (null == min || min.doubleValue() > num.doubleValue()) {
                        min = num;
                    }
                }
                return min;
            } else {
                Comparable min = null;
                for (Object d : data) {
                    if (!(d instanceof Comparable)) {
                        throw new IllegalArgumentException("data is not comparable: " + Arrays.toString(data));
                    }
                    if (null == min || min.compareTo(d) > 0) {
                        min = (Comparable) d;
                    }
                }
                return min;
            }
        }

        /**
         * 求总数
         *
         * @param data 数据
         * @return 和
         */
        public Double sum(List<Object> data) {
            return sum(data.toArray(new Object[0]));
        }

        /**
         * 求总数
         *
         * @param data 数据
         * @return 和
         */
        public Double sum(Object[] data) {
            Number[] numbers = toNumberArray(data);
            if (null == numbers) {
                throw new IllegalArgumentException("Unsupport data for 'ave' operation: " + Arrays.toString(data));
            }
            double total = 0d;
            for (Number n : numbers) {
                total += n.doubleValue();
            }
            return total;
        }

        /**
         * 取平均值
         *
         * @param data 数据
         * @return 均值
         */
        public Double ave(List<Object> data) {
            return ave(data.toArray(new Object[0]));
        }

        /**
         * 取平均值
         *
         * @param data 数据
         * @return 均值
         */
        public Double ave(Object[] data) {
            Number[] numbers = toNumberArray(data);
            if (null == numbers) {
                throw new IllegalArgumentException("Unsupport data for 'ave' operation: " + Arrays.toString(data));
            }
            double total = 0d;
            for (Number n : numbers) {
                total += n.doubleValue();
            }
            return total / data.length;
        }

        /**
         * 取方差（sum((x_i - mean)^2)/(n-1)）
         *
         * @param data 数据
         * @return 方差
         */
        public Double variance(List<Object> data) {
            return variance(data.toArray(new Object[0]));
        }

        /**
         * 取方差（sum((x_i - mean)^2)/(n-1)）
         *
         * @param data 数据
         * @return 方差
         */
        public Double variance(Object[] data) {
            Number[] numbers = toNumberArray(data);
            if (null == numbers) {
                throw new IllegalArgumentException("Unsupport data for 'ave' operation: " + Arrays.toString(data));
            }
            Double mean = ave(numbers);
            double total = 0d;
            for (Number n : numbers) {
                total += Math.pow(n.doubleValue() - mean, 2);
            }
            return total / (data.length - 1);
        }

        // --- 文本处理

        /**
         * 处理文本
         *
         * @param output       输出结果
         * @param includeLines 包含行
         * @param excludeLines 行除外
         * @return 解析数组
         */
        public String[][] parseTable(String output, int[] includeLines, int[] excludeLines) {
            List<String[]> result = new ArrayList<>();
            String[] lines = output.split("\\R");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (null != includeLines && includeLines.length > 0) {
                    if (!ArrayUtils.contains(includeLines, i)
                            && !ArrayUtils.contains(includeLines, i - lines.length)) {
                        continue;
                    }
                } else if (null != excludeLines && excludeLines.length > 0) {
                    if (ArrayUtils.contains(excludeLines, i)
                            || ArrayUtils.contains(excludeLines, i - lines.length)) {
                        continue;
                    }
                }
                String[] values = line.trim().split("[ \t]+");
                result.add(values);
            }
            return result.toArray(new String[0][]);
        }

        /**
         * 处理文本
         *
         * @param output       输出结果
         * @param includeLines 包含行
         * @param excludeLines 行除外
         * @param keyIndex     键值
         * @return 解析数组
         */
        public Map<String, String[]> parseTable(String output, int keyIndex, int[] includeLines, int[] excludeLines) {
            Map<String, String[]> result = new LinkedHashMap<>();
            String[] lines = output.split("\\R");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (null != includeLines && includeLines.length > 0) {
                    if (!ArrayUtils.contains(includeLines, i)
                            && !ArrayUtils.contains(includeLines, i - lines.length)) {
                        continue;
                    }
                } else if (null != excludeLines && excludeLines.length > 0) {
                    if (ArrayUtils.contains(excludeLines, i)
                            || ArrayUtils.contains(excludeLines, i - lines.length)) {
                        continue;
                    }
                }
                String[] values = line.trim().split("[ \t]+");
                String key;
                if (keyIndex >= 0) {
                    key = values[keyIndex];
                } else {
                    key = values[values.length + keyIndex];
                }
                result.put(key, values);
            }
            return result;
        }

        /**
         * 转换table
         *
         * @param output       原始输出
         * @param keyIndex     建索引
         * @param valueIndex   值索引
         * @param includeLines 包含行
         * @param excludeLines 行除外
         * @return 处理结果
         */
        public Map<String, String> parseTable(String output, int keyIndex, int valueIndex, int[] includeLines,
                                              int[] excludeLines) {
            Map<String, String> result = new LinkedHashMap<>();
            String[] lines = output.split("\\R");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (null != includeLines && includeLines.length > 0) {
                    if (!ArrayUtils.contains(includeLines, i)
                            && !ArrayUtils.contains(includeLines, i - lines.length)) {
                        continue;
                    }
                } else if (null != excludeLines && excludeLines.length > 0) {
                    if (ArrayUtils.contains(excludeLines, i)
                            || ArrayUtils.contains(excludeLines, i - lines.length)) {
                        continue;
                    }
                }
                String[] values = line.trim().split("[ \t]+");
                String key;
                if (keyIndex >= 0) {
                    key = values[keyIndex];
                } else {
                    key = values[values.length + keyIndex];
                }
                String value;
                if (valueIndex >= 0) {
                    value = values[valueIndex];
                } else {
                    value = values[values.length + valueIndex];
                }
                result.put(key, value);
            }
            return result;
        }

        /**
         * table 处理
         *
         * @param output       原始输出
         * @param keyIndex     建索引
         * @param includeLines 包含行
         * @param excludeLines 行除外
         * @param size         大小
         * @return 处理结果
         */
        public Map<String, String[]> defectiveTable(String output, int keyIndex, int[] includeLines,
                                                    int[] excludeLines, int size) {
            return defectiveTable(output, keyIndex, includeLines, excludeLines, size, false);
        }

        /**
         * 解决命令回显错行
         *
         * @param output       原始输入
         * @param keyIndex     作为map key的列索引
         * @param includeLines 需要处理的行数
         * @param excludeLines 不需要处理的行数
         * @param columns      空格分割后的列数
         * @param giveup       跟列数长度不一致的是否需要处理 false需要处理 true直接扔掉不处理
         * @return 结果 Map&lt;String, String[]&gt;
         */
        public Map<String, String[]> defectiveTable(String output, int keyIndex, int[] includeLines,
                                                    int[] excludeLines, int columns, boolean giveup) {
            Map<String, String[]> result = new LinkedHashMap<>();
            String[] lines = output.split("\\R");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (null != includeLines && includeLines.length > 0) {
                    if (!ArrayUtils.contains(includeLines, i)
                            && !ArrayUtils.contains(includeLines, i - lines.length)) {
                        continue;
                    }
                } else if (null != excludeLines && excludeLines.length > 0) {
                    if (ArrayUtils.contains(excludeLines, i)
                            || ArrayUtils.contains(excludeLines, i - lines.length)) {
                        continue;
                    }
                }

                String[] values = line.trim().split("[ \t]+");
                // 如果列的长度不=指定长度代表错行了
                if (values.length < columns) {
                    if (giveup) {
                        continue;
                    }
                    i += 1;
                    String[] nextValues = lines[i].trim().split("[ \t]+");
                    if (nextValues.length != columns) {
                        values = ArrayUtils.addAll(values, nextValues);
                    } else {
                        // 如果次行是正常格式则抛弃此行，直接处理
                        values = nextValues;
                    }
                }

                // 处理有包含空格的字符串 tmpfs 380M 0 380M 0% /run/user/"system d_001"
                if (values.length > columns) {
                    String lastElement = StringUtils
                            .join(ArrayUtils.subarray(values, columns - 1, values.length), " ");
                    String[] temArray = new String[columns];
                    for (int j = 0; j < columns; j++) {
                        if (j == columns - 1) {
                            temArray[j] = lastElement;
                        } else {
                            temArray[j] = values[j];
                        }
                    }
                    values = temArray;
                }

                String key;
                if (keyIndex >= 0) {
                    key = values[keyIndex];
                } else {
                    key = values[values.length + keyIndex];
                }
                result.put(key, values);
            }
            return result;
        }

        /**
         * 转换table
         *
         * @param output       原始输出
         * @param keyIndex     建索引
         * @param valueIndex   值索引
         * @param includeLines 包含行
         * @param excludeLines 行除外
         * @param size         大小
         * @return 处理结果
         */
        public Map<String, String> defectiveTable(String output, int keyIndex, int valueIndex, int[] includeLines,
                                                  int[] excludeLines, int size) {
            return defectiveTable(output, keyIndex, valueIndex, includeLines, excludeLines, size, false);
        }

        /**
         * 解决命令回显错行
         *
         * @param output       原始输入
         * @param keyIndex     作为map key的列索引
         * @param valueIndex   作为map value的列索引
         * @param includeLines 需要处理的行数
         * @param excludeLines 不需要处理的行数
         * @param columns      空格分割后的列数
         * @param giveup       跟列数长度不一致的是否需要处理 false需要处理 true直接扔掉不处理
         * @return Map&lt;String, String&gt;
         */
        public Map<String, String> defectiveTable(String output, int keyIndex, int valueIndex, int[] includeLines,
                                                  int[] excludeLines, int columns, boolean giveup) {
            Map<String, String> result = new LinkedHashMap<>();
            String[] lines = output.split("\\R");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (null != includeLines && includeLines.length > 0) {
                    if (!ArrayUtils.contains(includeLines, i)
                            && !ArrayUtils.contains(includeLines, i - lines.length)) {
                        continue;
                    }
                } else if (null != excludeLines && excludeLines.length > 0) {
                    if (ArrayUtils.contains(excludeLines, i)
                            || ArrayUtils.contains(excludeLines, i - lines.length)) {
                        continue;
                    }
                }
                String[] values = line.trim().split("[ \t]+");
                // 如果列的长度不=指定长度代表错行了
                if (values.length < columns) {
                    if (giveup) {
                        continue;
                    }
                    i += 1;
                    String[] nextValues = lines[i].trim().split("[ \t]+");
                    if (nextValues.length != columns) {
                        values = ArrayUtils.addAll(values, nextValues);
                    } else {
                        // 如果次行是正常格式则抛弃此行，直接处理
                        values = nextValues;
                    }
                }
                // 处理有包含空格的字符串 tmpfs 380M 0 380M 0% /run/user/"system d_001"
                if (values.length > columns) {
                    String lastElement = StringUtils
                            .join(ArrayUtils.subarray(values, columns - 1, values.length), " ");
                    String[] temArray = new String[columns];
                    for (int j = 0; j < columns; j++) {
                        if (j == columns - 1) {
                            temArray[j] = lastElement;
                        } else {
                            temArray[j] = values[j];
                        }
                    }
                    values = temArray;
                }
                String key;
                if (keyIndex >= 0) {
                    key = values[keyIndex];
                } else {
                    key = values[values.length + keyIndex];
                }
                String value;
                if (valueIndex >= 0) {
                    value = values[valueIndex];
                } else {
                    value = values[values.length + valueIndex];
                }
                result.put(key, value);
            }
            return result;
        }

        /**
         * 获取文本最后一列作为集合返回，从参考值位置开始截取到最后作为列值
         *
         * @param output 输入文本
         * @param refer  参考值
         * @return List
         */
        public List<String> listByHeader(String output, String refer) {
            return listByHeader(output, refer, null, false);
        }

        /**
         * 获取文本最后一列作为集合返回，从参考值位置开始截取到最后作为列值
         *
         * @param output   输入文本
         * @param refer    参考值
         * @param excludes 需要过滤的数据
         * @return List
         */
        public List<String> listByHeader(String output, String refer, String[] excludes) {
            return listByHeader(output, refer, excludes, false);
        }

        /**
         * 获取文本最后一列作为集合返回，从参考值位置开始截取到最后作为列值
         *
         * @param output       输入文本
         * @param refer        参考值
         * @param excludes     需要过滤的数据
         * @param includeRefer 是否包括第一行
         * @return List
         */
        public List<String> listByHeader(String output, String refer, String[] excludes, boolean includeRefer) {
            List<String> list = new ArrayList<>();
            String[] outputs = output.split("\\R");
            if (outputs.length > 1) {
                int index = outputs[0].indexOf(refer);
                if (index == -1) {
                    return list;
                }
                for (int i = 0; i < outputs.length; i++) {
                    if (!includeRefer && i == 0) {
                        continue;
                    }
                    if (outputs[i].length() > index) {
                        String dest = outputs[i].substring(index).trim();
                        if (excludes != null && ArrayUtils.contains(excludes, dest)) {
                            continue;
                        }
                        if (StringUtils.isBlank(dest)) {
                            continue;
                        }
                        list.add(dest);
                    }
                }
            }
            return list;
        }

        /**
         * 获取列数据
         *
         * @param data  数据
         * @param index 索引
         * @return 结果
         */
        public Object[] column(Collection<Object[]> data, int index) {
            return column(data.toArray(new Object[0][]), index);
        }

        /**
         * 获取列数据
         *
         * @param data  数据
         * @param index 索引
         * @return 结果
         */
        public Object[] column(Object[][] data, int index) {
            Object[] result = new Object[data.length];
            for (int i = 0; i < data.length; i++) {
                result[i] = index >= 0 ? data[i][index] : data[i][data[i].length + index];
            }
            return result;
        }

        /**
         * 将输出转换为行并返回
         *
         * @param output 结果
         * @return 转换结果
         */
        public String[] lines(String output) {
            return output.trim().split("\\R");
        }

        /**
         * 获取输出中的某行数据
         *
         * @param output 输出
         * @param index  索引
         * @return 数据
         */
        public String line(String output, int index) {
            String[] lines = output.split("\\R");
            return index >= 0 ? lines[index] : lines[lines.length + index];
        }

        /**
         * 获取正则表达式的值
         *
         * @param data  数据
         * @param regex 正则
         * @param group 分组
         * @return 匹配结果
         */
        public String matchGroup(String data, String regex, int group) {
            Matcher matcher = Pattern.compile(regex).matcher(data);
            return matcher.matches() ? matcher.group(group) : null;
        }

        /**
         * 剔除重复元素
         *
         * @param data 数据
         * @return 剔除结果
         */
        public Object[] distinct(Object[] data) {
            return distinct(Arrays.asList(data)).toArray();
        }

        /**
         * 剔除重复元素
         *
         * @param data 数据
         * @return 剔除结果
         */
        public List<Object> distinct(List<Object> data) {
            List<Object> list = new ArrayList<>();
            for (Object obj : data) {
                if (!list.contains(obj)) {
                    list.add(obj);
                }
            }
            return list;
        }

        /**
         * 是否包含操作
         *
         * @param obj   数据
         * @param array 数组
         * @return 是否包含
         */
        public boolean in(Object obj, Object[] array) {
            for (Object o : array) {
                if (obj == null && o == null || obj != null && obj.equals(o)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 是否包含操作
         *
         * @param obj  数据
         * @param list 数组
         * @return 是否包含
         */
        public boolean in(Object obj, List<Object> list) {
            return list.contains(obj);
        }

        /**
         * 将 xxT(B)/G(B)/M(B)/K(B) 的值转为字节数。
         *
         * @param value 数据
         * @return 字节
         */
        public long toBytes(String value) {
            value = value.trim().toUpperCase();
            if (value.endsWith("TB")) {
                return Long.parseLong(value.substring(0, value.length() - 2)) * 1099511627776L;
            } else if (value.endsWith("T")) {
                return Long.parseLong(value.substring(0, value.length() - 1)) * 1099511627776L;
            } else if (value.endsWith("GB")) {
                return Long.parseLong(value.substring(0, value.length() - 2)) * 1073741824;
            } else if (value.endsWith("G")) {
                return Long.parseLong(value.substring(0, value.length() - 1)) * 1073741824;
            } else if (value.endsWith("MB")) {
                return Long.parseLong(value.substring(0, value.length() - 2)) * 1048576;
            } else if (value.endsWith("M")) {
                return Long.parseLong(value.substring(0, value.length() - 1)) * 1048576;
            } else if (value.endsWith("KB")) {
                return Long.parseLong(value.substring(0, value.length() - 2)) * 1024;
            } else if (value.endsWith("K")) {
                return Long.parseLong(value.substring(0, value.length() - 1)) * 1024;
            } else if (value.endsWith("BYTE")) {
                return Long.parseLong(value.substring(0, value.length() - 4));
            } else if (value.endsWith("B")) {
                return Long.parseLong(value.substring(0, value.length() - 1));
            } else {
                return Long.parseLong(value);
            }
        }

        // --- 内部方法，不对外使用

        /**
         * 将特定类型的数组，尝试转换为 Double 类型的数组
         *
         * @param data 转换前的数据。
         * @return 转换后的数据。如果失败，返回 <code>null</code>
         */
        private Number[] toNumberArray(Object[] data) {
            try {
                Number[] result = new Number[data.length];
                for (int i = 0; i < data.length; i++) {
                    if (data[i] instanceof Number) {
                        result[i] = (Number) data[i];
                    } else if (data[i] instanceof String) {
                        String str = ((String) data[i]).trim();
                        if (NumberUtils.isParsable(str)) {
                            result[i] = Double.valueOf(str);
                        } else if (str.endsWith("%") && NumberUtils.isParsable(str.substring(0, str.length() - 1))) {
                            result[i] = NumberFormat.getPercentInstance().parse(str).doubleValue();
                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }
                }
                return result;
            } catch (Exception e) {
                return null;
            }
        }
    }
}
