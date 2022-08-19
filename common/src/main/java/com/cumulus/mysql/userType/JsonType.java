package com.cumulus.mysql.userType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * 自定义JSON类型
 *
 * @author zhaoff
 */
@Slf4j
public class JsonType implements UserType, ParameterizedType {

    public static final String JSON_TYPE_CLASS_PATH = "com.cumulus.mysql.userType.JsonType";

    /**
     * 参数名：类型
     */
    public static final String PARAM_NAME_TYPE = "type";

    /**
     * 参数名：元素
     */
    public static final String PARAM_NAME_ELEMENT = "element";

    /**
     * 参数名：处理器
     */
    public static final String PARAM_NAME_PROCESSOR = "processor";

    /**
     * 列表类型
     */
    public static final String LIST_TYPE = "LIST";

    /**
     * 队列类型
     */
    public static final String ARRAY_TYPE = "ARRAY";

    /**
     * JAVA对象映射JSON的映射器
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 对象列表的 reference
     */
    private static final TypeReference<List<?>> LIST_TYPE_REF = new TypeReference<List<?>>() {
    };

    /**
     * 对象队列的 reference
     */
    private static final TypeReference<Object[]> ARRAY_TYPE_REF = new TypeReference<Object[]>() {
    };

    /**
     * Java 类型
     */
    private JavaType valueType = null;

    /**
     * Class 类型
     */
    private Class<?> classType = null;

    /**
     * Json 类型的数据处理器
     */
    private JsonTypeProcessor processor;

    @Override
    public int[] sqlTypes() {
        return new int[]{Types.VARCHAR};
    }

    @Override
    public Class<?> returnedClass() {
        return classType;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        return Objects.hashCode(x);
    }

    @Override
    public Object nullSafeGet(ResultSet resultSet, String[] strings,
                              SharedSessionContractImplementor sharedSessionContractImplementor, Object o)
            throws HibernateException, SQLException {
        String jsonString = resultSet.getString(strings[0]);
        if (valueType == null) {
            throw new HibernateException("Value type not set.");
        }
        Object result = null;
        if (jsonString != null && !("").equals(jsonString.trim())) {
            try {
                result = MAPPER.readValue(jsonString, valueType);
                if (processor != null) {
                    result = processor.afterLoad(result);
                }
            } catch (IOException e) {
                throw new HibernateException("Exception deserializing value " + jsonString, e);
            }
        }
        return result;
    }

    @Override
    public void nullSafeSet(PreparedStatement preparedStatement, Object o, int i,
                            SharedSessionContractImplementor sharedSessionContractImplementor)
            throws HibernateException, SQLException {
        if (o == null) {
            preparedStatement.setNull(i, Types.OTHER);
        } else {
            try {
                if (processor != null) {
                    o = processor.beforeSave(o);
                }
                preparedStatement.setObject(i, MAPPER.writeValueAsString(o), Types.VARCHAR);
            } catch (JsonProcessingException e) {
                throw new HibernateException(e);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Object deepCopy(Object value) throws HibernateException {
        if (value == null) {
            return null;
        } else if (valueType.isCollectionLikeType()) {
            try {
                Collection<?> newValueCollection = (Collection<?>) value.getClass().newInstance();
                newValueCollection.addAll((Collection) value);
                return newValueCollection;
            } catch (Exception e) {
                throw new HibernateException("Failed to deep copy the collection-like value object", e);
            }
        } else if (valueType.isArrayType()) {
            try {
                int length = Array.getLength(value);
                Object obj = Array.newInstance(value.getClass().getComponentType(), length);
                for (int i = 0; i < length; i++) {
                    Array.set(obj, i, Array.get(value, i));
                }
                return obj;
            } catch (Exception e) {
                throw new HibernateException("Failed to deep copy the array-like value object", e);
            }
        }
        return value;
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) deepCopy(value);
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return deepCopy(cached);
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return deepCopy(original);
    }

    @Override
    public void setParameterValues(Properties parameters) {
        // 预处理器
        if (parameters != null && parameters.containsKey(PARAM_NAME_PROCESSOR)) {
            String className = parameters.getProperty(PARAM_NAME_PROCESSOR);
            try {
                processor = (JsonTypeProcessor) Class.forName(className).newInstance();
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error("Invalid JsonTypeProcessor: " + className, e);
                }
            }
        }

        // 缺省使用 Map 类型
        if (parameters == null || parameters.getProperty(PARAM_NAME_TYPE) == null) {
            classType = Map.class;
            valueType = MAPPER.getTypeFactory().constructType(Map.class);
            return;
        }

        // 从参数中获取类型信息
        String type = parameters.getProperty(PARAM_NAME_TYPE);
        String elemType = parameters.getProperty(PARAM_NAME_ELEMENT);
        if (LIST_TYPE.equals(type)) {
            // List 类型
            if (elemType != null) {
                try {
                    valueType = MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, Class.forName(elemType));
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("Type '" + elemType + "' is not a valid type.");
                }
            } else {
                valueType = MAPPER.getTypeFactory().constructType(LIST_TYPE_REF);
            }
            classType = List.class;
        } else if (ARRAY_TYPE.equals(type)) {
            // ARRAY 类型
            if (elemType != null) {
                try {
                    Class<?> elemClass = Class.forName(elemType);
                    valueType = MAPPER.getTypeFactory().constructArrayType(elemClass);
                    classType = Array.newInstance(elemClass, 0).getClass();
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("Type '" + elemType + "' is not a valid type.");
                }
            } else {
                valueType = MAPPER.getTypeFactory().constructType(ARRAY_TYPE_REF);
                classType = Array.newInstance(Object.class, 0).getClass();
            }
        } else {
            try {
                classType = Class.forName(type);
                valueType = MAPPER.getTypeFactory().constructType(classType);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Type '" + type + "' is not a valid type.");
            }
        }
    }
}
