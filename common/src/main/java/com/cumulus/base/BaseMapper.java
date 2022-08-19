package com.cumulus.base;

import java.util.List;

/**
 * 实体和传输对象映射接口
 */
public interface BaseMapper<D, E> {

    /**
     * 传输对象转实体
     *
     * @param dto 传输对象
     * @return 实体
     */
    E toEntity(D dto);

    /**
     * 实体转传输对象
     *
     * @param entity 实体
     * @return 传输对象
     */
    D toDto(E entity);

    /**
     * 传输对象列表转实体列表
     *
     * @param dtoList 传输对象列表
     * @return 实体列表
     */
    List<E> toEntity(List<D> dtoList);

    /**
     * 实体列表转传输对象列表
     *
     * @param entityList 实体列表
     * @return 传输对象列表
     */
    List<D> toDto(List<E> entityList);
    
}
