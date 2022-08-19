package com.cumulus.modules.business.gather.repository;

import com.cumulus.modules.business.gather.entity.mysql.GatherRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 采集记录数据访问接口
 *
 * @author zhangxq
 */
public interface GatherRecordRepository extends JpaRepository<GatherRecord, Long>, JpaSpecificationExecutor<GatherRecord> {

    /**
     * 统计采集结果
     *
     * @param result 采集结果
     * @return 数量
     */
    Long countByResultEquals(Integer result);
}
