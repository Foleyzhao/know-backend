package com.cumulus.mysql.userType;

/**
 * 对 Json 类型的实体在入库前/出库后进行处理的处理器接口
 *
 * @author zhaoff
 */
public interface JsonTypeProcessor {

    /**
     * 在入库前对数据进行处理
     *
     * @param data 处理的数据对象
     * @return 处理后的数据
     */
    Object beforeSave(Object data);

    /**
     * 在出库后对数据进行处理
     *
     * @param data 处理的数据对象
     * @return 处理后的数据
     */
    Object afterLoad(Object data);
}
