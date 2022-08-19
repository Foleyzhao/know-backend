package com.cumulus.modules.business.detect.repository;

import java.util.List;
import com.cumulus.modules.business.detect.entity.DetectRecord;
import com.cumulus.modules.business.detect.entity.RecordDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * 发现任务记录详情数据访问接口
 *
 * @author zhangxq
 */
public interface RecordDetailRepository extends JpaRepository<RecordDetail, Long>, JpaSpecificationExecutor<RecordDetail> {

    /**
     * 根据记录id查询详情
     *
     * @param record
     * @return
     */
    List<RecordDetail> findByDetectRecordEquals(DetectRecord record);
}
