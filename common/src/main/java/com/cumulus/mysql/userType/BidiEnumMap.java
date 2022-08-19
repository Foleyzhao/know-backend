package com.cumulus.mysql.userType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * 双向 Enum Map.
 *
 * @author zhaoff
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class BidiEnumMap implements Serializable {

    /**
     * 序列化ID
     */
    private static final long serialVersionUID = 3325751131102095834L;

    /**
     * 常量：获取ID
     */
    public static final String ENUM_ID_ACCESSOR = "getId";

    /**
     * 日志对象
     */
    private static final Log LOG = LogFactory.getLog(BidiEnumMap.class);

    /**
     * enum to key map
     */
    private final Map enumToKey;

    /**
     * key to enum map
     */
    private final Map keytoEnum;

    /**
     * 构造函数
     *
     * @param enumClass enum类
     * @throws NoSuchMethodException     NoSuchMethodException
     * @throws IllegalAccessException    IllegalAccessException
     * @throws InvocationTargetException InvocationTargetException
     */
    public BidiEnumMap(Class<?> enumClass)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Building Bidirectional Enum Map...");
        }

        EnumMap enumToKey = new EnumMap(enumClass);
        HashMap keytoEnum = new HashMap();

        Method idAccessor = getIdAccessor(enumClass);

        Method valuesAccessor = enumClass.getMethod("values");
        Object[] values = (Object[]) valuesAccessor.invoke(enumClass);

        for (Object value : values) {
            Object id = idAccessor.invoke(value);
            enumToKey.put((Enum) value, id);
            if (keytoEnum.containsKey(id)) {
                LOG.warn(String.format("Duplicate Enum ID '%s' detected for Enum %s!", id, enumClass.getName()));
            }
            keytoEnum.put(id, value);
        }

        this.enumToKey = Collections.unmodifiableMap(enumToKey);
        this.keytoEnum = Collections.unmodifiableMap(keytoEnum);
    }

    /**
     * getId方法构造器。
     *
     * @param enumClass enum类
     * @return 方法
     * @throws NoSuchMethodException NoSuchMethodException
     */
    private Method getIdAccessor(Class<?> enumClass) throws NoSuchMethodException {
        for (Method method : enumClass.getMethods()) {
            if (method.getName().equals(ENUM_ID_ACCESSOR)) {
                return method;
            }
        }
        return enumClass.getMethod("ordinal");
    }

    /**
     * 根据key获取enum值。
     *
     * @param id key
     * @return enum值
     */
    public Object getEnumValue(int id) {
        return keytoEnum.get(id);
    }

    /**
     * 根据enum值获取key值
     *
     * @param enumValue enum值
     * @return key值
     */
    public int getKey(Object enumValue) {
        return (Integer) enumToKey.get(enumValue);
    }
}
