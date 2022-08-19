package com.cumulus.repository;

import com.cumulus.entity.LocalStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 本地存储实体数据访问接口
 *
 * @author zhaoff
 */
public interface LocalStorageRepository extends JpaRepository<LocalStorage, Long>,
        JpaSpecificationExecutor<LocalStorage> {
}
