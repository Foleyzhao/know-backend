package com.cumulus.modules.business.repository;

import com.cumulus.modules.business.entity.AssetExtend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 资产扩展属性数据访问接口
 *
 * @author zhangxq
 */
public interface AssetExtendRepository extends JpaRepository<AssetExtend, Long>, JpaSpecificationExecutor<AssetExtend> {
}
