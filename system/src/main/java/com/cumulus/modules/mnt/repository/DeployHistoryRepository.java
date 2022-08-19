package com.cumulus.modules.mnt.repository;

import com.cumulus.modules.mnt.entity.DeployHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 部署历史数据访问接口
 */
public interface DeployHistoryRepository extends JpaRepository<DeployHistory, String>,
        JpaSpecificationExecutor<DeployHistory> {
}
