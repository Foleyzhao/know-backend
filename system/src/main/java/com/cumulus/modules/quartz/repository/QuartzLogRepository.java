package com.cumulus.modules.quartz.repository;

import com.cumulus.modules.quartz.entity.QuartzLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 定时任务日志数据访问接口
 *
 * @author zhaoff
 */
public interface QuartzLogRepository extends JpaRepository<QuartzLog, Long>, JpaSpecificationExecutor<QuartzLog> {

}
