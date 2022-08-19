package com.cumulus.modules.business.gather.repository;

import com.cumulus.modules.business.gather.entity.mysql.GatherPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 采集周期数据访问接口
 *
 * @author zhaoff
 */
public interface GatherPeriodRepository extends JpaRepository<GatherPeriod, Long>,
        JpaSpecificationExecutor<GatherPeriod> {
}
