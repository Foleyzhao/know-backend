package com.cumulus.modules.business.detect.repository;

import com.cumulus.modules.business.detect.entity.DetectRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 发现记录数据访问接口
 *
 * @author zhangxq
 */
public interface DetectRecordRepository extends JpaRepository<DetectRecord, Long>,
        JpaSpecificationExecutor<DetectRecord> {
}
