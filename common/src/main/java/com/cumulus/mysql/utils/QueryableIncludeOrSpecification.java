package com.cumulus.mysql.utils;

import com.google.common.primitives.Primitives;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 支持多条件查询的 JPA Specification
 *
 * @param <T> 查询的根实体类型
 * @author zhaoff
 */
@Slf4j
public class QueryableIncludeOrSpecification<T> implements Specification<T> {

    private static final long serialVersionUID = 7785566959145107417L;

    /**
     * 使用左连接关联查询的属性名前缀。例如：LJ_users.userNameLike
     */
    public static final String LEFT_JOIN_PREFIX = "LJ_";

    /**
     * 所有支持的查询关键字。
     */
    private static final List<String> KEYWORDS;

    static {
        // 初始化所有查询条件关键字
        List<String> keywordList = new ArrayList<>();
        // 注意：Is 和 Not 必须在前面，否则匹配可能会有问题，例如 In 如果在 NotIn 前面，就会误当做字段类型
        // AFTER
        keywordList.add("IsAfter");
        keywordList.add("After");
        // BEFORE
        keywordList.add("IsBefore");
        keywordList.add("Before");
        // CONTAINING
        keywordList.add("IsContaining");
        keywordList.add("Containing");
        keywordList.add("Contains");
        // BETWEEN
        keywordList.add("IsBetween");
        keywordList.add("Between");
        // ENDING_WITH
        keywordList.add("IsEndingWith");
        keywordList.add("EndingWith");
        keywordList.add("EndsWith");
        // FALSE
        keywordList.add("IsFalse");
        keywordList.add("False");
        // GREATER THAN
        keywordList.add("IsGreaterThan");
        keywordList.add("GreaterThan");
        // GREATER_THAN_EQUALS
        keywordList.add("IsGreaterThanEqual");
        keywordList.add("GreaterThanEqual");
        // IS
        keywordList.add("Is");
        keywordList.add("Equals");
        // NOT_NULL
        keywordList.add("IsNotNull");
        keywordList.add("NotNull");
        // NULL
        keywordList.add("IsNull");
        keywordList.add("Null");
        // LESS_THAN
        keywordList.add("IsLessThan");
        keywordList.add("LessThan");
        // LESSS_THAN_EQUAL
        keywordList.add("IsLessThanEqual");
        keywordList.add("LessThanEqual");
        // NOT
        keywordList.add("IsNot");
        keywordList.add("Not");
        // NOT_IN
        keywordList.add("IsNotIn");
        keywordList.add("NotIn");
        // IN
        keywordList.add("IsIn");
        keywordList.add("In");
        // NOT_LIKE
        keywordList.add("IsNotLike");
        keywordList.add("NotLike");
        // LIKE
        keywordList.add("IsLike");
        keywordList.add("Like");
        // STARTING_WITH
        keywordList.add("IsStartingWith");
        keywordList.add("StartingWith");
        keywordList.add("StartsWith");
        // TRUE
        keywordList.add("IsTrue");
        keywordList.add("True");
        // 线程保护并赋值
        KEYWORDS = Collections.synchronizedList(keywordList);
    }

    /**
     * 包含日期、时间、时区的格式化对象。
     */
    private static final SimpleDateFormat DATE_FORMAT_ZONE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

    /**
     * 包含日期、时间的格式化对象。
     */
    private static final SimpleDateFormat DATA_FORMAT_SEC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 仅包含日期的格式化对象。
     */
    private static final SimpleDateFormat DATA_FORMAT_DAY = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 封装查询条件。
     */
    private static class Query {
        /**
         * 查询关键字。
         */
        String keyword;
        /**
         * 查询字段类型。
         */
        Class<?> type;
        /**
         * 扩展的类型信息，当字段类型标注了 @Type 时有用，处理 jsonb/json/array 等类型。
         */
        Map<String, String[]> extTypeInfo;
        /**
         * 查询的单值条件。
         */
        Object value;
        /**
         * 查询的多值条件（用于In， Between等）。
         */
        Object[] values;
    }

    /**
     * 查询的根实体类型。
     */
    private Class<T> entityType;

    /**
     * 所有查询条件。
     */
    private MultiValueMap<String, Query> queries = new LinkedMultiValueMap<>();

    /**
     * or查询条件
     */
    private MultiValueMap<String, Query> orQueries = new LinkedMultiValueMap<>();

    /**
     * 多个查询条件间是否使用 OR 连接。
     */
    private boolean useOr = false;

    /**
     * 对 Like 查询中的通配符（% 和 _）进行转义。
     */
    private boolean escapeLikeWildcard = true;

    /**
     * 是否去重
     */
    private boolean distinct = true;

    /**
     * 是否存在多对多关联关系查询。
     */
    private boolean hasManyToMany = false;

    /**
     * 构造查询 Specification。
     *
     * @param entityType 查询的实体类型。
     * @param params     包含所有查询条件的参数。
     */
    public QueryableIncludeOrSpecification(Class<T> entityType, MultiValueMap<String, String> params) {
        this.entityType = entityType;
        generateQueries(params);
    }

    /**
     * 构造查询 Specification(规范)。
     *
     * @param entityType 查询的实体类型。
     * @param params     包含所有查询条件的参数。
     * @param useOr      多个查询条件间是否使用 OR 连接。
     */
    public QueryableIncludeOrSpecification(Class<T> entityType, MultiValueMap<String, String> params,
                                           boolean useOr) {
        this(entityType, params);
        this.useOr = useOr;
    }

    /**
     * 构造查询 Specification(规范)。
     *
     * @param entityType            查询的实体类型。
     * @param params                包含所有查询条件的参数。
     * @param useOr                 多个查询条件间是否使用 OR 连接。
     * @param notEscapeLikeWildcard 是否不对 Like 查询中的通配符（% 和 _）进行转义。
     */
    public QueryableIncludeOrSpecification(Class<T> entityType, MultiValueMap<String, String> params,
                                           boolean useOr, boolean notEscapeLikeWildcard) {
        this(entityType, params, useOr);
        this.escapeLikeWildcard = !notEscapeLikeWildcard;
    }

    /**
     * 构造查询 Specification(规范)。
     *
     * @param entityType 查询的实体类型。
     * @param andParams  and连接查询条件
     * @param orParams   or连接查询条件
     */
    public QueryableIncludeOrSpecification(Class<T> entityType, MultiValueMap<String, String> andParams,
                                           MultiValueMap<String, String> orParams) {
        this.entityType = entityType;
        generateQueries(orParams);
        orQueries.putAll(queries);
        queries.clear();
        generateQueries(andParams);
    }

    /**
     * 构造查询 Specification(规范)。
     *
     * @param distinct   是否去重。默认true。
     * @param entityType 查询的实体类型。
     * @param andParams  and连接查询条件
     * @param orParams   or连接查询条件
     */
    public QueryableIncludeOrSpecification(boolean distinct, Class<T> entityType,
                                           MultiValueMap<String, String> andParams, MultiValueMap<String, String> orParams) {
        this.entityType = entityType;
        this.distinct = distinct;
        generateQueries(orParams);
        orQueries.putAll(queries);
        queries.clear();
        generateQueries(andParams);
    }


    /**
     * 根据参数，生成查询条件。
     *
     * @param params 包含查询条件的参数。
     */
    private void generateQueries(MultiValueMap<String, String> params) {
        for (Map.Entry<String, List<String>> param : params.entrySet()) {
            String name = param.getKey();
            if ("page".equals(name) || "size".equals(name) || "sort".equals(name)
                    || name.startsWith("_")) {
                // 分页排序条件及控制参数（以下划线开头），忽略
                continue;
            }

            // 查询关键字
            String keyword = null;
            for (String kw : KEYWORDS) {
                if (name.endsWith(kw)) {
                    keyword = kw;
                    name = name.substring(0, name.length() - kw.length());
                    break;
                }
            }
            Map<String, String[]> extTypeInfo = new HashMap<>(8);
            Class<?> type = getNestedType(name, extTypeInfo);
            if (type == null) {
                log.warn(String.format("Type is null and can't find (nested) field '%s'.", name));
                continue;
            }
            for (String value : param.getValue()) {
                Query query = new Query();
                query.keyword = keyword;
                query.type = type;
                if (!extTypeInfo.isEmpty()) {
                    name = StringUtils.join(extTypeInfo.remove("__NAME__"), ".");
                    query.extTypeInfo = extTypeInfo;
                }
                if ("False".equals(keyword) || "IsFalse".equals(keyword)
                        || "True".equals(keyword) || "IsTrue".equals(keyword)
                        || "NotNull".equals(keyword) || "IsNotNull".equals(keyword)
                        || "Null".equals(keyword) || "IsNull".equals(keyword)) {
                    // 一元运算符，无需转换值
                    queries.add(name, query);
                } else if ("Between".equals(keyword) || "IsBetween".equals(keyword)
                        || "In".equals(keyword) || "IsIn".equals(keyword)
                        || "NotIn".equals(keyword) || "IsNotIn".equals(keyword)) {
                    // 多元运算符，分解并转换以“ - ”（仅支持 Between，注意横线左右均有空格）或以“,”分隔的值
                    query.values = splitAndConvert(type, query.extTypeInfo, value, keyword);
                    if (query.values != null) {
                        if ("Between".equals(keyword) || "IsBetween".equals(keyword)) {
                            if (query.values.length != 2) {
                                log.warn("'Between' requires 2 operands, but get: " + value);
                                continue;
                            } else if (query.values[0] == null && query.values[1] == null) {
                                log.warn("The 2 operands of 'Between' cannot both be null.");
                                continue;
                            } else if (query.values[0] == null) {
                                query.keyword = "LessThanEqual";
                                query.value = query.values[1];
                            } else if (query.values[1] == null) {
                                query.keyword = "GreaterThanEqual";
                                query.value = query.values[0];
                            }
                        }
                        queries.add(name, query);
                    }
                } else {
                    // 其他均为二元运算符，转换单值
                    query.value = convert(type, query.extTypeInfo, value);
                    if (query.value != null) {
                        queries.add(name, query);
                    }
                }
            }
        }
    }

    /**
     * 获取实体的特定访问路径下的字段类型。
     *
     * @param path    访问路径。
     * @param extInfo 扩展信息，当字段为 json/jsonb/array 时，保存子路径等信息。
     * @return 字段类型。如果找不到该字段，返回 <code>null</code>。
     */
    private Class<?> getNestedType(String path, Map<String, String[]> extInfo) {
        String[] arr = path.split("\\.");
        Class<?> type = entityType;
        for (int i = 0; i < arr.length; i++) {
            String tempPath = arr[i];
            if (tempPath.startsWith(LEFT_JOIN_PREFIX)) {
                // 这个字段使用 Left Out Join，类型应该为集合类型
                tempPath = tempPath.substring(LEFT_JOIN_PREFIX.length());
            }
            try {
                Field field = type.getDeclaredField(tempPath);
                // 返回所表示字段的声明类型
                type = field.getType();
                Type typeAnnotation = field.getAnnotation(Type.class);
                if (typeAnnotation != null) {
                    // 处理 PostgreSQL 的特殊类型
                    String[] strArr = Arrays.copyOfRange(arr, i + 1, arr.length);
                    extInfo.put(typeAnnotation.type(), strArr);
                    extInfo.put("__NAME__", Arrays.copyOfRange(arr, 0, i + 1));
                    break;
                } else if (Collection.class.isAssignableFrom(type)) {
                    // 判断此Class对象所表示的类或接口与指定的Class参数所表示的类或接口是否相同,或是否是其超类或超接口
                    // 处理集合类型
                    type = getElementType(field);
                    if (type == null) {
                        return null;
                    }
                }
            } catch (NoSuchFieldException e) {
                return null;
            }
        }
        return type;
    }

    /**
     * 对集合类型的字段，从 @OneToMany 或 @ManyToMany 的 targetEntity 属性读取元素类型。
     *
     * @param field Collection 类型的字段。
     * @return 元素类型。
     */
    private Class<?> getElementType(Field field) {
        OneToMany otm = field.getAnnotation(OneToMany.class);
        if (otm != null) {
            Class<?> cls = otm.targetEntity();
            if (cls != null) {
                return cls;
            }
        } else {
            ManyToMany mtm = field.getAnnotation(ManyToMany.class);
            Class<?> cls = mtm.targetEntity();
            if (cls != null) {
                hasManyToMany = true;
                return cls;
            }
        }
        log.warn("For collection type, you must specify 'targetEntity' attribute for"
                + " @OneToMany or @ManyToMany annotation");
        return null;
    }

    /**
     * 将特定值以“ - ”或“,”分隔，并将每个分隔的元素转换为特定类型。分隔时考虑转义符“\”。<br>
     * 当操作符关键字为“Between”或“IsBetween”且分隔符为“ - ”时，允许左值或右值为空。
     *
     * @param type        转换的类型。
     * @param extTypeInfo 扩展的类型信息，当为 json/jsonb/array 时有用。
     * @param value       以“ - ”或“,”分隔的待转换值。
     * @param keyword     操作符关键字，仅当其值为“Between”或“IsBetween”时，才处理“ - ”分隔符。
     * @return 转换结果。如果某个元素转换失败，则返回 <code>null</code>。
     * 该方法确保不会返回长度为 0 的数组。
     */
    private Object[] splitAndConvert(Class<?> type, Map<String, String[]> extTypeInfo,
                                     String value, String keyword) {
        // 保存结果
        List<Object> list = new ArrayList<>();

        // 处理 Between 的特殊分隔符情况
        int idx = value.indexOf(" - ");
        if (("Between".equals(keyword) || "IsBetween".equals(keyword)) && idx != -1) {
            String tempValue = value.substring(0, idx).trim();
            list.add(tempValue.length() == 0 ? null : convert(type, extTypeInfo, tempValue));
            tempValue = value.substring(idx + 3).trim();
            list.add(tempValue.length() == 0 ? null : convert(type, extTypeInfo, tempValue));
            return list.toArray(new Object[0]);
        }

        // 处理其他情况
        idx = 0;
        int from = 0;
        while (true) {
            idx = value.indexOf(',', from);
            while (idx > 0 && value.charAt(idx - 1) == '\\') {
                // 转义的分隔符，忽略，找下一个
                idx = value.indexOf(',', idx + 1);
            }
            if (idx != -1) {
                Object obj = convert(type, extTypeInfo, value.substring(from, idx));
                if (obj == null) {
                    return null;
                }
                list.add(obj);
                from = idx + 1;
            } else {
                Object obj = convert(type, extTypeInfo, value.substring(from));
                if (obj == null) {
                    return null;
                }
                // 这里确保 list 的长度必然大于 0
                list.add(obj);
                break;
            }
        }
        return list.toArray(new Object[0]);
    }

    /**
     * 将字符串转换为特定类型。
     *
     * @param type        转换的目标类型。
     * @param extTypeInfo 扩展的类型信息，当为 json/jsonb/array 时有用。
     * @param value       字符串值。
     * @return 转换后的值。如果转换失败，返回 <code>null</code>。
     */
    private Object convert(Class<?> type, Map<String, String[]> extTypeInfo, String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        if (type.equals(String.class)) {
            return value;
        } else if (type.equals(Date.class)) {
            try {
                if (value.length() > 19) {
                    synchronized (DATE_FORMAT_ZONE) {
                        return DATE_FORMAT_ZONE.parse(value);
                    }
                } else if (value.length() > 10) {
                    synchronized (DATA_FORMAT_SEC) {
                        return DATA_FORMAT_SEC.parse(value);
                    }
                } else {
                    synchronized (DATA_FORMAT_DAY) {
                        return DATA_FORMAT_DAY.parse(value);
                    }
                }
            } catch (ParseException pe) {
                log.warn("Invalid datetime value: " + value);
                return null;
            }
        }
        if (extTypeInfo != null) {
            // 对扩展类型，直接以字符串类型返回，在后面做处理
            return value;
        }
        if (type.isPrimitive()) {
            type = Primitives.wrap(type);
        }
        Method method = null;
        try {
            method = type.getMethod("valueOf", String.class);
        } catch (NoSuchMethodException ignore) {
            // ignore
        }
        if (method != null) {
            try {
                return method.invoke(null, value);
            } catch (Exception e) {
                log.warn(String.format("Failed to convert '%s' to type '%s'.",
                        value, type.getName()), e);
                return null;
            }
        }
        // 其他类型暂不支持
        log.warn("Unsupported type: " + type.getName());
        return null;
    }

    /**
     * Map Equal比较。
     *
     * @param map1 map1
     * @param map2 map2
     * @return 结果
     */
    private boolean mapEquals(Map<String, String[]> map1, Map<String, String[]> map2) {
        if (map1.size() != map2.size()) {
            return false;
        } else if (map1.keySet().equals(map2.keySet())) {
            return false;
        }
        for (Map.Entry<String, String[]> entry : map1.entrySet()) {
            String key = entry.getKey();
            if (!Arrays.equals(map1.get(key), map2.get(key))) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        // 剔重
        if (distinct || hasManyToMany) {
            query.distinct(true);
        }

        if (queries.isEmpty() && orQueries.isEmpty()) {
            return null;
        }

        // 如果获取 “总记录数” 不需要包含过滤条件（即获取表中真实记录数，而不是过滤后的记录数），可去掉注释
        /*
        Class<?> clazz = query.getResultType();
        if (clazz.equals(Long.class) || clazz.equals(long.class)) {
            // Pageable search for total records
            return null;
        }
        */

        List<Predicate> predicates = new ArrayList<>();
        // 遍历orQueries
        List<Predicate> useOrPredicates = new ArrayList<>();
        for (Map.Entry<String, List<Query>> entry : orQueries.entrySet()) {
            String propName = entry.getKey();
            List<Query> queryList = entry.getValue();
            for (Query tempQuery : queryList) {
                useOrPredicates.add(toPredicate(propName, tempQuery, root, cb));
            }
        }
        if (useOrPredicates.size() > 1) {
            predicates.add(cb.or(useOrPredicates.toArray(new Predicate[0])));
        } else if (useOrPredicates.size() == 1) {
            predicates.add(useOrPredicates.get(0));
        }

        // 遍历queries
        for (Map.Entry<String, List<Query>> entry : queries.entrySet()) {
            String propName = entry.getKey();
            List<Query> queryList = entry.getValue();
            if (queryList.size() > 1) {
                // 多个同名条件之间，如果关键字相同，则关系为“OR”；如果关键字不同，则关系为“AND”
                List<Query> tmp = new ArrayList<>(queryList);
                while (tmp.size() > 0) {
                    Iterator<Query> iter = tmp.iterator();
                    String keyword = null;
                    Map<String, String[]> extTypeInfo = null;
                    List<Predicate> orPredicates = new ArrayList<>();
                    while (iter.hasNext()) {
                        Query tempQuery = iter.next();
                        if (tempQuery.extTypeInfo == null) {
                            if (keyword == null || keyword.equals(tempQuery.keyword)) {
                                // keyword 相同，为“OR”关系
                                iter.remove();
                                Predicate predicate = toPredicate(propName, tempQuery, root, cb);
                                orPredicates.add(predicate);
                            }
                        } else {
                            if (keyword == null || keyword.equals(tempQuery.keyword) && extTypeInfo != null
                                    && mapEquals(extTypeInfo, tempQuery.extTypeInfo)) {
                                // 当 extTypeInfo 不为 null（使用了 PostgreSQL 的 JSONB 传递查询条件），
                                // 除了 keyword 相同外，还需确保 extTypeInfo 相同，才为“OR”关系
                                iter.remove();
                                Predicate predicate = toPredicate(propName, tempQuery, root, cb);
                                orPredicates.add(predicate);
                            }
                            extTypeInfo = tempQuery.extTypeInfo;
                        }
                        keyword = tempQuery.keyword;
                    }
                    if (orPredicates.size() > 1) {
                        predicates.add(cb.or(orPredicates.toArray(new Predicate[0])));
                    } else if (orPredicates.size() == 1) {
                        predicates.add(orPredicates.get(0));
                    }
                }
            } else {
                Predicate predicate = toPredicate(propName, queryList.get(0), root, cb);
                if (predicate != null) {
                    predicates.add(predicate);
                }
            }
        }

        if (useOr) {
            return query.where(cb.or(predicates.toArray(new Predicate[0]))).getRestriction();
        } else {
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        }
    }

    /**
     * 生成单个查询条件。
     *
     * @param propName 查询属性名。
     * @param q        查询条件。
     * @param root     根实体节点。
     * @param cb       查询条件构造器。
     * @return 查询条件。
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate toPredicate(String propName, Query q, Root<T> root, CriteriaBuilder cb) {
        if (q.keyword == null || "Is".equals(q.keyword) || "Equals".equals(q.keyword)) {
            if (q.extTypeInfo != null) {
                // PostgreSQL 特殊类型扩展
                Map.Entry<String, String[]> entry = q.extTypeInfo.entrySet().iterator().next();
                String extType = entry.getKey();
                if ("com.shterm.pgsql.usertype.InetType".equals(extType)) {
                    if (InetAddress.class.isAssignableFrom(q.type)) {
                        try {
                            return cb.equal(toExpression(propName, root),
                                    InetAddress.getByName((String) q.value));
                        } catch (UnknownHostException e) {
                            log.warn("Invalid InetAddress value: " + q.value, e);
                            return null;
                        }
                    } else {
                        return cb.equal(toExpression(propName, root), q.value);
                    }
                } else if (!"com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType)
                        && !"com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType)) {
                    log.warn("Invalid SQL type for equals operation: " + extType);
                    return null;
                }
                String[] path = entry.getValue();
                if (path.length == 0) {
                    log.warn("json/jsonb path length can not be 0");
                    return null;
                }
                Expression<String>[] exps = new Expression[path.length + 1];
                exps[0] = (Expression<String>) toExpression(propName, root);
                for (int i = 0; i < path.length; i++) {
                    exps[i + 1] = cb.literal(path[i]);
                }
                boolean isJsonb = "com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType);
                // 数据库中保存的 Json 值，可能同时包含数字和字符，如果条件中的值为数字，
                // 但数据库中的值包含字符，可能报错，故注释掉
                /*
                try {
                    Integer v = Integer.valueOf((String) q.value);
                    return cb.equal(cb.function(isJsonb ? "jsonb_extract_path_int"
                        : "json_extract_path_int", Integer.class, exps), v);
                } catch (NumberFormatException ignore) {}
                try {
                    Long v = Long.valueOf((String) q.value);
                    return cb.equal(cb.function(isJsonb ? "jsonb_extract_path_bigint"
                        : "json_extract_path_bigint", Long.class, exps), v);
                } catch (NumberFormatException ignore) {}
                try {
                    Double v = Double.valueOf((String) q.value);
                    return cb.equal(cb.function(isJsonb ? "jsonb_extract_path_double"
                        : "json_extract_path_double", Double.class, exps), v);
                } catch (NumberFormatException ignore) {}
                */
                return cb.equal(cb.function(isJsonb ? "jsonb_extract_path_text"
                        : "json_extract_path_text", String.class, exps), q.value);
            }
            return cb.equal(toExpression(propName, root), q.value);
        } else if ("Not".equals(q.keyword) || "IsNot".equals(q.keyword)) {
            if (q.extTypeInfo != null) {
                // PostgreSQL 特殊类型扩展
                Map.Entry<String, String[]> entry = q.extTypeInfo.entrySet().iterator().next();
                String extType = entry.getKey();
                if ("com.shterm.pgsql.usertype.InetType".equals(extType)) {
                    if (InetAddress.class.isAssignableFrom(q.type)) {
                        try {
                            return cb.notEqual(toExpression(propName, root),
                                    InetAddress.getByName((String) q.value));
                        } catch (UnknownHostException e) {
                            log.warn("Invalid InetAddress value: " + q.value, e);
                            return null;
                        }
                    } else {
                        return cb.notEqual(toExpression(propName, root), q.value);
                    }
                } else if (!"com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType)
                        && !"com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType)) {
                    log.warn("Invalid SQL type for notEquals operation: " + extType);
                    return null;
                }
                String[] path = entry.getValue();
                if (path.length == 0) {
                    log.warn("json/jsonb path length can not be 0");
                    return null;
                }
                Expression<String>[] exps = new Expression[path.length + 1];
                exps[0] = (Expression<String>) toExpression(propName, root);
                for (int i = 0; i < path.length; i++) {
                    exps[i + 1] = cb.literal(path[i]);
                }
                boolean isJsonb = "com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType);
                // 数据库中保存的 Json 值，可能同时包含数字和字符，如果条件中的值为数字，
                // 但数据库中的值包含字符，可能报错，故注释掉
                /*
                try {
                    Integer v = Integer.valueOf((String) q.value);
                    return cb.equal(cb.function(isJsonb ? "jsonb_extract_path_int"
                        : "json_extract_path_int", Integer.class, exps), v);
                } catch (NumberFormatException ignore) {}
                try {
                    Long v = Long.valueOf((String) q.value);
                    return cb.equal(cb.function(isJsonb ? "jsonb_extract_path_bigint"
                        : "json_extract_path_bigint", Long.class, exps), v);
                } catch (NumberFormatException ignore) {}
                try {
                    Double v = Double.valueOf((String) q.value);
                    return cb.equal(cb.function(isJsonb ? "jsonb_extract_path_double"
                        : "json_extract_path_double", Double.class, exps), v);
                } catch (NumberFormatException ignore) {}
                */
                return cb.notEqual(cb.function(isJsonb ? "jsonb_extract_path_text"
                        : "json_extract_path_text", String.class, exps), q.value);
            }
            // 为 Null 的情况也作为不等于进行处理
            return cb.or(cb.notEqual(toExpression(propName, root, true), q.value),
                    cb.isNull(toExpression(propName, root, true)));
        } else if ("True".equals(q.keyword) || "IsTrue".equals(q.keyword)) {
            return cb.isTrue((Expression<Boolean>) toExpression(propName, root));
        } else if ("False".equals(q.keyword) || "IsFalse".equals(q.keyword)) {
            return cb.isFalse((Expression<Boolean>) toExpression(propName, root));
        } else if ("Null".equals(q.keyword) || "IsNull".equals(q.keyword)) {
            // 为 null 需要使用左连接进行查询
            if (q.extTypeInfo != null) {
                Map.Entry<String, String[]> entry = q.extTypeInfo.entrySet().iterator().next();
                String extType = entry.getKey();
                String[] path = entry.getValue();
                if ("com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType)
                        || "com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType)) {
                    if (path.length == 0) {
                        log.warn("json/jsonb path length can not be 0");
                        return null;
                    }
                    Expression<String>[] exps = new Expression[path.length + 1];
                    exps[0] = (Expression<String>) toExpression(propName, root);
                    for (int i = 0; i < path.length; i++) {
                        exps[i + 1] = cb.literal(path[i]);
                    }
                    boolean isJsonb = "com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType);
                    try {
                        return cb.isNull(cb.function(isJsonb ? "jsonb_extract_path_text"
                                : "json_extract_path_text", String.class, exps));
                    } catch (Exception ignore) {
                        // ignore
                    }
                }
            }
            return cb.isNull(toExpression(propName, root, true));
        } else if ("NotNull".equals(q.keyword) || "IsNotNull".equals(q.keyword)) {
            if (q.extTypeInfo != null) {
                Map.Entry<String, String[]> entry = q.extTypeInfo.entrySet().iterator().next();
                String extType = entry.getKey();
                String[] path = entry.getValue();
                if ("com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType)
                        || "com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType)) {
                    if (path.length == 0) {
                        log.warn("json/jsonb path length can not be 0");
                        return null;
                    }
                    Expression<String>[] exps = new Expression[path.length + 1];
                    exps[0] = (Expression<String>) toExpression(propName, root);
                    for (int i = 0; i < path.length; i++) {
                        exps[i + 1] = cb.literal(path[i]);
                    }
                    boolean isJsonb = "com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType);
                    try {
                        return cb.isNotNull(cb.function(isJsonb ? "jsonb_extract_path_text"
                                : "json_extract_path_text", String.class, exps));
                    } catch (Exception ignore) {
                        // ignore
                    }
                }
            }
            return cb.isNotNull(toExpression(propName, root));
        } else if ("Before".equals(q.keyword) || "IsBefore".equals(q.keyword)
                || "LessThan".equals(q.keyword) || "IsLessThan".equals(q.keyword)) {
            if (Number.class.isAssignableFrom(q.type)) {
                return cb.lt((Expression<Number>) toExpression(propName, root), (Number) q.value);
            } else if (Date.class.isAssignableFrom(q.type)) {
                return cb.lessThan((Expression<Date>) toExpression(propName, root), (Date) q.value);
            } else if (String.class.isAssignableFrom(q.type)) {
                return cb.lessThan((Expression<String>) toExpression(propName, root),
                        (String) q.value);
            } else if (q.extTypeInfo != null) {
                // json/jsonb 扩展（注意：inet 类型只支持 String 类型映射，前面已经处理）
                Map.Entry<String, String[]> entry = q.extTypeInfo.entrySet().iterator().next();
                String extType = entry.getKey();
                if (!"com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType)
                        && !"com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType)) {
                    log.warn("Invalid SQL type for comparation operation: " + extType);
                    return null;
                }
                String[] path = entry.getValue();
                if (path.length == 0) {
                    log.warn("json/jsonb path length can not be 0");
                    return null;
                }
                Expression<String>[] exps = new Expression[path.length + 1];
                exps[0] = (Expression<String>) toExpression(propName, root);
                for (int i = 0; i < path.length; i++) {
                    exps[i + 1] = cb.literal(path[i]);
                }
                boolean isJsonb = "com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType);
                try {
                    Integer var = Integer.valueOf((String) q.value);
                    return cb.lt(cb.function(isJsonb ? "jsonb_extract_path_int"
                            : "json_extract_path_int", Integer.class, exps), var);
                } catch (NumberFormatException ignore) {
                    // ignore
                }
                try {
                    Long var = Long.valueOf((String) q.value);
                    return cb.lt(cb.function(isJsonb ? "jsonb_extract_path_bigint"
                            : "json_extract_path_bigint", Long.class, exps), var);
                } catch (NumberFormatException ignore) {
                    // ignore
                }
                try {
                    Double var = Double.valueOf((String) q.value);
                    return cb.lt(cb.function(isJsonb ? "jsonb_extract_path_double"
                            : "json_extract_path_double", Double.class, exps), var);
                } catch (NumberFormatException ignore) {
                    // ignore
                }
                return cb.lessThan(cb.function(isJsonb ? "jsonb_extract_path_text"
                        : "json_extract_path_text", String.class, exps), (String) q.value);
            } else {
                log.warn("Unsupport value type '" + q.type.getName() + "(" + propName + ")"
                        + "' for query keyword: Before/IsBefore/LessThan/IsLessThan");
                return null;
            }
        } else if ("After".equals(q.keyword) || "IsAfter".equals(q.keyword)
                || "GreaterThan".equals(q.keyword) || "IsGreaterThan".equals(q.keyword)) {
            if (Number.class.isAssignableFrom(q.type)) {
                return cb.gt((Expression<Number>) toExpression(propName, root), (Number) q.value);
            } else if (Date.class.isAssignableFrom(q.type)) {
                return cb.greaterThan((Expression<Date>) toExpression(propName, root),
                        (Date) q.value);
            } else if (String.class.isAssignableFrom(q.type)) {
                return cb.greaterThan((Expression<String>) toExpression(propName, root),
                        (String) q.value);
            } else if (q.extTypeInfo != null) {
                // json/jsonb 扩展（注意：inet 类型只支持 String 类型映射，前面已经处理）
                Map.Entry<String, String[]> entry = q.extTypeInfo.entrySet().iterator().next();
                String extType = entry.getKey();
                if (!"com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType)
                        && !"com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType)) {
                    log.warn("Invalid SQL type for comparation operation: " + extType);
                    return null;
                }
                String[] path = entry.getValue();
                if (path.length == 0) {
                    log.warn("json/jsonb path length can not be 0");
                    return null;
                }
                Expression<String>[] exps = new Expression[path.length + 1];
                exps[0] = (Expression<String>) toExpression(propName, root);
                for (int i = 0; i < path.length; i++) {
                    exps[i + 1] = cb.literal(path[i]);
                }
                boolean isJsonb = "com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType);
                try {
                    Integer var = Integer.valueOf((String) q.value);
                    return cb.gt(cb.function(isJsonb ? "jsonb_extract_path_int"
                            : "json_extract_path_int", Integer.class, exps), var);
                } catch (NumberFormatException ignore) {
                    // ignore
                }
                try {
                    Long var = Long.valueOf((String) q.value);
                    return cb.gt(cb.function(isJsonb ? "jsonb_extract_path_bigint"
                            : "json_extract_path_bigint", Long.class, exps), var);
                } catch (NumberFormatException ignore) {
                    // ignore
                }
                try {
                    Double var = Double.valueOf((String) q.value);
                    return cb.gt(cb.function(isJsonb ? "jsonb_extract_path_double"
                            : "json_extract_path_double", Double.class, exps), var);
                } catch (NumberFormatException ignore) {
                    // ignore
                }
                return cb.greaterThan(cb.function(isJsonb ? "jsonb_extract_path_text"
                        : "json_extract_path_text", String.class, exps), (String) q.value);
            } else {
                log.warn("Unsupport value type '" + q.type.getName() + "(" + propName + ")"
                        + "' for query keyword: After/IsAfter/GreaterThan/IsGreaterThan");
                return null;
            }
        } else if ("LessThanEqual".equals(q.keyword) || "IsLessThanEqual".equals(q.keyword)) {
            if (Number.class.isAssignableFrom(q.type)) {
                return cb.le((Expression<Number>) toExpression(propName, root), (Number) q.value);
            } else if (Date.class.isAssignableFrom(q.type)) {
                return cb.lessThanOrEqualTo((Expression<Date>) toExpression(propName, root),
                        (Date) q.value);
            } else if (String.class.isAssignableFrom(q.type)) {
                return cb.lessThanOrEqualTo((Expression<String>) toExpression(propName, root),
                        (String) q.value);
            } else if (q.extTypeInfo != null) {
                // json/jsonb 扩展（注意：inet 类型只支持 String 类型映射，前面已经处理）
                Map.Entry<String, String[]> entry = q.extTypeInfo.entrySet().iterator().next();
                String extType = entry.getKey();
                if (!"com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType)
                        && !"com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType)) {
                    log.warn("Invalid SQL type for comparation operation: " + extType);
                    return null;
                }
                String[] path = entry.getValue();
                if (path.length == 0) {
                    log.warn("json/jsonb path length can not be 0");
                    return null;
                }
                Expression<String>[] exps = new Expression[path.length + 1];
                exps[0] = (Expression<String>) toExpression(propName, root);
                for (int i = 0; i < path.length; i++) {
                    exps[i + 1] = cb.literal(path[i]);
                }
                boolean isJsonb = "com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType);
                try {
                    Integer var = Integer.valueOf((String) q.value);
                    return cb.le(cb.function(isJsonb ? "jsonb_extract_path_int"
                            : "json_extract_path_int", Integer.class, exps), var);
                } catch (NumberFormatException ignore) {
                    // ignore
                }
                try {
                    Long var = Long.valueOf((String) q.value);
                    return cb.le(cb.function(isJsonb ? "jsonb_extract_path_bigint"
                            : "json_extract_path_bigint", Long.class, exps), var);
                } catch (NumberFormatException ignore) {
                    // ignore
                }
                try {
                    Double var = Double.valueOf((String) q.value);
                    return cb.le(cb.function(isJsonb ? "jsonb_extract_path_double"
                            : "json_extract_path_double", Double.class, exps), var);
                } catch (NumberFormatException ignore) {
                    // ignore
                }
                return cb.lessThanOrEqualTo(cb.function(isJsonb ? "jsonb_extract_path_text"
                        : "json_extract_path_text", String.class, exps), (String) q.value);
            } else {
                log.warn("Unsupport value type '" + q.type.getName() + "(" + propName + ")"
                        + "' for query keyword: LessThanEqual/IsLessThanEqual");
                return null;
            }
        } else if ("GreaterThanEqual".equals(q.keyword) || "IsGreaterThanEqual".equals(q.keyword)) {
            if (Number.class.isAssignableFrom(q.type)) {
                return cb.ge((Expression<Number>) toExpression(propName, root), (Number) q.value);
            } else if (Date.class.isAssignableFrom(q.type)) {
                return cb.greaterThanOrEqualTo((Expression<Date>) toExpression(propName, root),
                        (Date) q.value);
            } else if (String.class.isAssignableFrom(q.type)) {
                return cb.greaterThanOrEqualTo((Expression<String>) toExpression(propName, root),
                        (String) q.value);
            } else if (q.extTypeInfo != null) {
                // json/jsonb 扩展（注意：inet 类型只支持 String 类型映射，前面已经处理）
                Map.Entry<String, String[]> entry = q.extTypeInfo.entrySet().iterator().next();
                String extType = entry.getKey();
                if (!"com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType)
                        && !"com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType)) {
                    log.warn("Invalid SQL type for comparation operation: " + extType);
                    return null;
                }
                String[] path = entry.getValue();
                if (path.length == 0) {
                    log.warn("json/jsonb path length can not be 0");
                    return null;
                }
                Expression<String>[] exps = new Expression[path.length + 1];
                exps[0] = (Expression<String>) toExpression(propName, root);
                for (int i = 0; i < path.length; i++) {
                    exps[i + 1] = cb.literal(path[i]);
                }
                boolean isJsonb = "com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType);
                try {
                    Integer var = Integer.valueOf((String) q.value);
                    return cb.ge(cb.function(isJsonb ? "jsonb_extract_path_int"
                            : "json_extract_path_int", Integer.class, exps), var);
                } catch (NumberFormatException ignore) {
                    // ignore
                }
                try {
                    Long var = Long.valueOf((String) q.value);
                    return cb.ge(cb.function(isJsonb ? "jsonb_extract_path_bigint"
                            : "json_extract_path_bigint", Long.class, exps), var);
                } catch (NumberFormatException ignore) {
                    // ignore
                }
                try {
                    Double var = Double.valueOf((String) q.value);
                    return cb.ge(cb.function(isJsonb ? "jsonb_extract_path_double"
                            : "json_extract_path_double", Double.class, exps), var);
                } catch (NumberFormatException ignore) {
                    // ignore
                }
                return cb.greaterThanOrEqualTo(cb.function(isJsonb ? "jsonb_extract_path_text"
                        : "json_extract_path_text", String.class, exps), (String) q.value);
            } else {
                log.warn("Unsupport value type '" + q.type.getName() + "(" + propName + ")"
                        + "' for query keyword: GreaterThanEqual/IsGreaterThanEqual");
                return null;
            }
        } else if ("Like".equals(q.keyword) || "IsLike".equals(q.keyword)
                || "Containing".equals(q.keyword) || "IsContaining".equals(q.keyword)
                || "Contains".equals(q.keyword)) {
            if (String.class.equals(q.type)) {
                if (q.extTypeInfo != null
                        && q.extTypeInfo.containsKey("com.shterm.pgsql.usertype.InetType")) {
                    return cb.like(cb.function("host", String.class,
                            (Expression<String>) toExpression(propName, root)),
                            '%' + escape(q.value) + '%');
                }
                return cb.like(cb.lower((Expression<String>) toExpression(propName, root)),
                        '%' + escape(q.value).toLowerCase() + '%');
            } else if (q.extTypeInfo != null) {
                // PostgreSQL 特殊类型扩展
                Map.Entry<String, String[]> entry = q.extTypeInfo.entrySet().iterator().next();
                String extType = entry.getKey();
                String[] path = entry.getValue();
                if ("com.shterm.pgsql.usertype.InetType".equals(extType)) {
                    return cb.like(cb.function("host", String.class,
                            (Expression<String>) toExpression(propName, root)),
                            '%' + escape(q.value).toLowerCase() + '%');
                } else if ("com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType)
                        || "com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType)) {
                    if (path.length == 0) {
                        log.warn("json/jsonb path length can not be 0");
                        return null;
                    }
                    Expression<String>[] exps = new Expression[path.length + 1];
                    exps[0] = (Expression<String>) toExpression(propName, root);
                    for (int i = 0; i < path.length; i++) {
                        exps[i + 1] = cb.literal(path[i]);
                    }
                    boolean isJsonb = "com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType);
                    return cb.like(cb.lower(cb.function(isJsonb ? "jsonb_extract_path_text"
                                    : "json_extract_path_text", String.class, exps)),
                            '%' + escape(q.value).toLowerCase() + '%');
                } else if ("com.shterm.pgsql.usertype.ArrayType".equals(extType) && (
                        "Containing".equals(q.keyword) || "IsContaining".equals(q.keyword)
                                || "Contains".equals(q.keyword))) {
                    return cb.isNotNull(cb.function("array_position", Integer.class,
                            (Expression<String>) toExpression(propName, root),
                            cb.literal((String) q.value)));
                } else {
                    log.warn("Unsupported type '" + extType + "' with path: "
                            + Arrays.toString(path));
                    return null;
                }
            } else {
                log.warn("Unsupport value type '" + q.type.getName() + "(" + propName + ")"
                        + "' for query keyword: Like/IsLike/Containing/IsContaining/Contains");
                return null;
            }
        } else if ("NotLike".equals(q.keyword) || "IsNotLike".equals(q.keyword)) {
            if (String.class.equals(q.type)) {
                if (q.extTypeInfo != null
                        && q.extTypeInfo.containsKey("com.shterm.pgsql.usertype.InetType")) {
                    return cb.notLike(cb.function("host", String.class,
                            (Expression<String>) toExpression(propName, root)),
                            '%' + escape(q.value) + '%');
                }
                return cb.notLike(cb.lower((Expression<String>) toExpression(propName, root)),
                        '%' + escape(q.value).toLowerCase() + '%');
            } else if (q.extTypeInfo != null) {
                // PostgreSQL 特殊类型扩展
                Map.Entry<String, String[]> entry = q.extTypeInfo.entrySet().iterator().next();
                String extType = entry.getKey();
                String[] path = entry.getValue();
                if ("com.shterm.pgsql.usertype.InetType".equals(extType)) {
                    return cb.notLike(cb.function("host", String.class,
                            (Expression<String>) toExpression(propName, root)),
                            '%' + escape(q.value).toLowerCase() + '%');
                } else if ("com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType)
                        || "com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType)) {
                    if (path.length == 0) {
                        log.warn("json/jsonb path length can not be 0");
                        return null;
                    }
                    Expression<String>[] exps = new Expression[path.length + 1];
                    exps[0] = cb.lower((Expression<String>) toExpression(propName, root));
                    for (int i = 0; i < path.length; i++) {
                        exps[i + 1] = cb.literal(path[i]);
                    }
                    boolean isJsonb = "com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType);
                    return cb.notLike(cb.function(isJsonb ? "jsonb_extract_path_text"
                                    : "json_extract_path_text", String.class, exps),
                            '%' + escape(q.value).toLowerCase() + '%');
                } else {
                    log.warn("Unsupported type '" + extType + "'");
                    return null;
                }
            } else {
                log.warn("Unsupport value type '" + q.type.getName() + "(" + propName + ")"
                        + "' for query keyword: NotLike/IsNotLike");
                return null;
            }
        } else if ("StartingWith".equals(q.keyword) || "IsStartingWith".equals(q.keyword)
                || "StartsWith".equals(q.keyword)) {
            if (String.class.equals(q.type)) {
                if (q.extTypeInfo != null
                        && q.extTypeInfo.containsKey("com.shterm.pgsql.usertype.InetType")) {
                    return cb.like(cb.function("host", String.class,
                            (Expression<String>) toExpression(propName, root)), escape(q.value) + '%');
                }
                return cb.like(cb.lower((Expression<String>) toExpression(propName, root)),
                        escape(q.value).toLowerCase() + '%');
            } else if (q.extTypeInfo != null) {
                // PostgreSQL 特殊类型扩展
                Map.Entry<String, String[]> entry = q.extTypeInfo.entrySet().iterator().next();
                String extType = entry.getKey();
                String[] path = entry.getValue();
                if ("com.shterm.pgsql.usertype.InetType".equals(extType)) {
                    return cb.like(cb.function("host", String.class,
                            (Expression<String>) toExpression(propName, root)),
                            escape(q.value).toLowerCase() + '%');
                } else if ("com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType)
                        || "com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType)) {
                    if (path.length == 0) {
                        log.warn("json/jsonb path length can not be 0");
                        return null;
                    }
                    Expression<String>[] exps = new Expression[path.length + 1];
                    exps[0] = cb.lower((Expression<String>) toExpression(propName, root));
                    for (int i = 0; i < path.length; i++) {
                        exps[i + 1] = cb.literal(path[i]);
                    }
                    boolean isJsonb = "com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType);
                    return cb.like(cb.function(isJsonb ? "jsonb_extract_path_text"
                                    : "json_extract_path_text", String.class, exps),
                            escape(q.value).toLowerCase() + '%');
                } else {
                    log.warn("Unsupported type '" + extType + "'");
                    return null;
                }
            } else {
                log.warn("Unsupport value type '" + q.type.getName() + "(" + propName + ")"
                        + "' for query keyword: StartingWith/IsStartingWith/StartsWith");
                return null;
            }
        } else if ("EndingWith".equals(q.keyword) || "IsEndingWith".equals(q.keyword)
                || "EndsWith".equals(q.keyword)) {
            if (String.class.equals(q.type)) {
                if (q.extTypeInfo != null
                        && q.extTypeInfo.containsKey("com.shterm.pgsql.usertype.InetType")) {
                    return cb.like(cb.function("host", String.class,
                            (Expression<String>) toExpression(propName, root)), '%' + escape(q.value));
                }
                return cb.like(cb.lower((Expression<String>) toExpression(propName, root)),
                        '%' + escape(q.value).toLowerCase());
            } else if (q.extTypeInfo != null) {
                // PostgreSQL 特殊类型扩展
                Map.Entry<String, String[]> entry = q.extTypeInfo.entrySet().iterator().next();
                String extType = entry.getKey();
                String[] path = entry.getValue();
                if ("com.shterm.pgsql.usertype.InetType".equals(extType)) {
                    return cb.like(cb.function("host", String.class,
                            cb.lower((Expression<String>) toExpression(propName, root))),
                            '%' + escape(q.value).toLowerCase());
                } else if ("com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType)
                        || "com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType)) {
                    if (path.length == 0) {
                        log.warn("json/jsonb path length can not be 0");
                        return null;
                    }
                    Expression<String>[] exps = new Expression[path.length + 1];
                    exps[0] = cb.lower((Expression<String>) toExpression(propName, root));
                    for (int i = 0; i < path.length; i++) {
                        exps[i + 1] = cb.literal(path[i]);
                    }
                    boolean isJsonb = "com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType);
                    return cb.like(cb.function(isJsonb ? "jsonb_extract_path_text"
                                    : "json_extract_path_text", String.class, exps),
                            '%' + escape(q.value).toLowerCase());
                } else {
                    log.warn("Unsupported type '" + extType + "'");
                    return null;
                }
            } else {
                log.warn("Unsupport value type '" + q.type.getName() + "(" + propName + ")"
                        + "' for query keyword: EndingWith/IsEndingWith/EndsWith");
                return null;
            }
        } else if ("Between".equals(q.keyword) || "IsBetween".equals(q.keyword)) {
            if (Comparable.class.isAssignableFrom(q.type)) {
                return cb.between((Expression<Comparable>) toExpression(propName, root),
                        (Comparable) q.values[0], (Comparable) q.values[1]);
            } else if (q.extTypeInfo != null) {
                // json/jsonb 扩展（注意：inet 类型只支持 String 类型映射，前面已经处理）
                Map.Entry<String, String[]> entry = q.extTypeInfo.entrySet().iterator().next();
                String extType = entry.getKey();
                String[] path = entry.getValue();
                if ("com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType)
                        || "com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType)) {
                    if (path.length == 0) {
                        log.warn("json/jsonb path length can not be 0");
                        return null;
                    }
                    Expression<String>[] exps = new Expression[path.length + 1];
                    exps[0] = (Expression<String>) toExpression(propName, root);
                    for (int i = 0; i < path.length; i++) {
                        exps[i + 1] = cb.literal(path[i]);
                    }
                    boolean isJsonb = "com.yuan.server.scan.common.mysql.userType.JsonType".equals(extType);
                    try {
                        Integer v0 = Integer.valueOf((String) q.values[0]);
                        Integer v1 = Integer.valueOf((String) q.values[1]);
                        return cb.between(cb.function(isJsonb ? "jsonb_extract_path_int"
                                : "json_extract_path_int", Integer.class, exps), v0, v1);
                    } catch (NumberFormatException ignore) {
                        // ignore
                    }
                    try {
                        Long v0 = Long.valueOf((String) q.values[0]);
                        Long v1 = Long.valueOf((String) q.values[1]);
                        return cb.between(cb.function(isJsonb ? "jsonb_extract_path_bigint"
                                : "json_extract_path_bigint", Long.class, exps), v0, v1);
                    } catch (NumberFormatException ignore) {
                        // ignore
                    }
                    try {
                        Double v0 = Double.valueOf((String) q.values[0]);
                        Double v1 = Double.valueOf((String) q.values[1]);
                        return cb.between(cb.function(isJsonb ? "jsonb_extract_path_double"
                                : "json_extract_path_double", Double.class, exps), v0, v1);
                    } catch (NumberFormatException ignore) {
                        // ignore
                    }
                    return cb.between(cb.function(isJsonb ? "jsonb_extract_path_text"
                                    : "json_extract_path_text", String.class, exps),
                            (String) q.values[0], (String) q.values[1]);
                } else {
                    log.warn("Unsupported type '" + extType + "'");
                    return null;
                }
            } else {
                log.warn("Unsupport value type '" + q.type.getName() + "(" + propName + ")"
                        + "' for query keyword: Between/IsBetween");
                return null;
            }
        } else if ("In".equals(q.keyword) || "IsIn".equals(q.keyword)) {
            return toExpression(propName, root).in(q.values);
        } else if ("NotIn".equals(q.keyword) || "IsNotIn".equals(q.keyword)) {
            // 为 Null 的情况也作为不等于进行处理
            return cb.or(cb.not(toExpression(propName, root, true).in(q.values)),
                    cb.isNull(toExpression(propName, root, true)));
        }
        log.error("Unsupported query keyword: " + q.keyword);
        return null;
    }

    /**
     * 对 Like 查询中的通配符（% 和 _）进行转义。该方法根据 {@link #escapeLikeWildcard} 属性配置进行处理。
     *
     * @param value 转义前的值。
     * @return 转义后的值。
     */
    private String escape(Object value) {
        String result = (String) value;
        if (result != null && escapeLikeWildcard) {
            return result.replace("\\", "\\\\").replaceAll("\\x25", "\\\\%").replaceAll("\\x5f", "\\\\_");
        } else {
            return result;
        }
    }

    /**
     * 循环获取查询属性值的查询路径。
     *
     * @param propName 属性路径，可能为 x.y.z 格式。
     * @param root     查询根节点。
     * @return 查询路径。
     */
    private Expression<?> toExpression(String propName, Root<T> root) {
        return toExpression(propName, root, false);
    }

    /**
     * 循环获取查询属性值的查询路径。
     *
     * @param propName 属性路径，可能为 x.y.z 格式。
     * @param root     查询根节点。
     * @param leftJoin 是否使用左连接进行查询。
     * @return 查询路径。
     */
    private Expression<?> toExpression(String propName, Root<T> root, boolean leftJoin) {
        String[] arr = propName.split("\\.");
        Path<?> path = null;
        for (String p : arr) {
            boolean forceLeftJoin = false;
            if (p.startsWith(LEFT_JOIN_PREFIX)) {
                // 这个字段使用 Left Out Join，类型应该为集合类型
                forceLeftJoin = true;
                p = p.substring(LEFT_JOIN_PREFIX.length());
            }
            if (path == null) {
                try {
                    if (Collection.class.isAssignableFrom(
                            entityType.getDeclaredField(p).getType())) {
                        if (leftJoin || forceLeftJoin) {
                            path = root.join(p, JoinType.LEFT);
                        } else {
                            path = root.join(p);
                        }
                    } else {
                        path = root.get(p);
                    }
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            } else {
                path = path.get(p);
            }
        }
        return path;
    }
}
