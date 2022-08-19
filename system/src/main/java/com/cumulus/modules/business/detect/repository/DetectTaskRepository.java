package com.cumulus.modules.business.detect.repository;

import com.cumulus.modules.business.detect.entity.DetectTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * 发现任务数据访问接口
 *
 * @author zhangxq
 */
public interface DetectTaskRepository extends JpaRepository<DetectTask, Long>, JpaSpecificationExecutor<DetectTask> {

    /**
     * 根据任务ID更新任务状态
     * 0未开始 1正在执行 2暂停 3执行结束
     *
     * @param taskStatus 任务状态
     * @param id         任务ID
     */
    @Modifying
    @Transactional
    @Query(value = "update tbl_detect_task set task_status = ?1 where id = ?2 ", nativeQuery = true)
    void updateStatusById(Integer taskStatus, Long id);


    /**
     * 根据任务名称查询
     *
     * @return 数量
     */
    int countByDetectTaskNameEquals(String name);
}
