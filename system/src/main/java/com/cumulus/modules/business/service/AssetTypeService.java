package com.cumulus.modules.business.service;

import java.util.List;
import java.util.Set;
import com.cumulus.modules.business.dto.AssetSysTypeDto;
import com.cumulus.modules.business.dto.AssetTypeDto;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;


/**
 * 资产类型服务接口
 */
public interface AssetTypeService {

    /**
     * 批量加入
     *
     * @param file 资产类型列表
     * @return 批量录入结果
     */
    Object createBatch(MultipartFile file);

    /**
     * 新增资产类型
     *
     * @param assetSysTypeDto 资产对象
     */
    void create(AssetSysTypeDto assetSysTypeDto);

    /**
     * 新增父资产类型
     *
     * @param assetTypeDto 父资产类型
     */
    void create(AssetTypeDto assetTypeDto);

    /**
     * 分页查询资产类型
     *
     * @param pageable 分页参数
     * @return 资产类型列表
     */
    Object queryAll(Pageable pageable);

    /**
     * 查询全部
     *
     * @return 资产类型
     */
    Object querySelecct();

    /**
     * 分页查询资产类型
     *
     * @param id       父id
     * @param pageable 分页参数
     * @return 资产类型列表
     */
    Object queryChild(Integer id, Pageable pageable);

    /**
     * 根据id删除
     *
     * @param id id
     */
    void removeTypeById(Integer id);

    /**
     * 删除子类型
     *
     * @param id 子类型id
     */
    void removeSysTypeById(Integer id);

    /**
     * 批量删除
     *
     * @param ids    id列表
     * @param delAll 是否全部删除
     */
    void removeBatchType(Set<Integer> ids, Boolean delAll);

    /**
     * 批量删除
     *
     * @param ids    id列表
     * @param delAll 是否全部删除
     */
    void removeBatchSysType(Set<Integer> ids, Boolean delAll);

    /**
     * 全部删除
     *
     */
    void delAll();

    /**
     * 修改资产类型
     *
     * @param assetTypeDto 资产类型传输对象
     */
    void updateById(AssetTypeDto assetTypeDto);


    /**
     * 修改资产类型
     *
     * @param assetTypeDto 资产类型传输对象
     */
    void updateSysTypeById(AssetSysTypeDto assetTypeDto);


}
