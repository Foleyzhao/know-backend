package com.cumulus.modules.business.repository;

import java.util.Collection;
import java.util.List;
import com.cumulus.modules.business.entity.AssetSysType;
import com.cumulus.modules.business.entity.AssetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 资产类型数据访问接口
 */
public interface AssetTypeRepository extends JpaRepository<AssetType, Integer>, JpaSpecificationExecutor<AssetType> {

    /**
     * 根据名称查
     *
     * @param typename 资产名称
     * @return 资产类型
     */
    AssetType findAssetTypeByNameEquals(String typename);
}
