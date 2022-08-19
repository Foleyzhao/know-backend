package com.cumulus.modules.business.repository;

import com.cumulus.modules.business.gather.entity.mysql.GatherTaskStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 采集状态数据访问
 *
 * @author shijh
 */
public interface GatherTaskStatusRepository extends JpaRepository<GatherTaskStatus, Long>, JpaSpecificationExecutor<GatherTaskStatus> {
}
