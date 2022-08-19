package com.cumulus.modules.business.detect.service;


import java.util.List;
import com.cumulus.modules.business.detect.entity.RecordDetail;
import org.springframework.data.domain.Pageable;

/**
 * 发现记录服务接口
 *
 * @author zhangxq
 */
public interface DetectRecordService {


    /**
     * 根据任务id查询记录
     *
     * @param taskId   任务id
     * @param pageable 分页参数
     * @return 记录列表
     */
    Object queryByTaskId(Long taskId, Pageable pageable);

    /**
     * 根据记录id查询详情
     *
     * @param id
     * @return
     */
    List<RecordDetail> queryDetails(Long id);
}
