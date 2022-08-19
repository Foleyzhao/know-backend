package com.cumulus.modules.mnt.repository;

import com.cumulus.modules.mnt.entity.App;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 应用数据访问接口
 */
public interface AppRepository extends JpaRepository<App, Long>, JpaSpecificationExecutor<App> {
}
