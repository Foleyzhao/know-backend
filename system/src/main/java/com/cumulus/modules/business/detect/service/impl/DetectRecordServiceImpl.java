package com.cumulus.modules.business.detect.service.impl;

import java.util.List;
import javax.annotation.Resource;
import com.cumulus.modules.business.detect.entity.DetectRecord;
import com.cumulus.modules.business.detect.entity.RecordDetail;
import com.cumulus.modules.business.detect.mapper.DetectRecordMapper;
import com.cumulus.modules.business.detect.repository.DetectRecordRepository;
import com.cumulus.modules.business.detect.repository.RecordDetailRepository;
import com.cumulus.modules.business.detect.service.DetectRecordService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * 发现任务记录服务实现
 *
 * @author zhangxq
 */
@Service
public class DetectRecordServiceImpl implements DetectRecordService {

    /**
     * 发现记录数据访问接口
     */
    @Resource
    private DetectRecordRepository repository;

    /**
     * 记录详情数据访问接口
     */
    @Resource
    private RecordDetailRepository recordDetailRepository;

    /**
     * 发现记录传输对象与发现记录实体的映射
     */
    @Resource
    private DetectRecordMapper mapper;

    /**
     * 根据任务id查询记录
     *
     * @param taskId   任务id
     * @param pageable 分页参数
     * @return 记录列表
     */
    @Override
    public Object queryByTaskId(Long taskId, Pageable pageable) {
        Page<DetectRecord> detectRecordPage = repository.findAll((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("detectTaskId"), taskId), pageable);
        return detectRecordPage.map(mapper::toDto);
    }

    /**
     * 根据记录id查询详情
     *
     * @param id
     * @return
     */
    @Override
    public List<RecordDetail> queryDetails(Long id) {
        DetectRecord record = new DetectRecord();
        record.setId(id);
        return recordDetailRepository.findByDetectRecordEquals(record);
    }
}
