package com.cumulus.modules.business.repository;

import java.util.List;
import com.cumulus.modules.business.entity.AssetSysType;
import com.cumulus.modules.business.entity.AssetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * 资产类型数据访问
 *
 * @author Shijh
 */
public interface AssetSysTypeRepository extends JpaRepository<AssetSysType, Integer>, JpaSpecificationExecutor<AssetSysType> {

    /**
     * 查询资产类型
     *
     * @param id 一级类型
     * @return 结果集
     */
    @Query(value = "select * from tbl_asset_sys_type where asset_type_id=?1", nativeQuery = true)
    List<AssetSysType> queryAllByAssetTypeIs(Integer id);

    /**
     * 根据名称查
     *
     * @param typename 资产名称
     * @return 资产类型
     */
    AssetSysType findAssetSysTypeByNameEquals(String typename);

    /**
     * 根据名称和父类型查
     *
     * @param typename
     * @param parent
     * @return
     */
    AssetSysType findAssetSysTypeByNameEqualsAndAssetTypeEquals(String typename,AssetType parent);

    /**
     * 根据父类型查询子类型
     *
     * @param assetType 父资产类型
     * @return 子资产类型列表
     */
    Page<AssetSysType> findAssetSysTypesByAssetTypeEquals(AssetType assetType, Pageable pageable);

    /**
     * 查询子类型个数
     *
     * @param assetType 父资产类型
     * @return 子资产类型数量
     */
    Integer countAssetSysTypesByAssetTypeEquals(AssetType assetType);
}
