package com.cumulus.modules.quartz.repository;

import com.cumulus.modules.quartz.entity.QuartzJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;

/**
 * 定时任务数据访问接口
 *
 * @author zhaoff
 */
public interface QuartzJobRepository extends JpaRepository<QuartzJob, Long>, JpaSpecificationExecutor<QuartzJob> {

    /**
     * 查询启用的定时任务
     *
     * @return 定时任务列表
     */
    List<QuartzJob> findByIsPauseIsFalse();

    /**
     * 根据任务名获取任务列表
     *
     * @param jobType 任务类型
     * @return 返回列表
     */
    List<QuartzJob> findAllByJobTypeInAndIsPauseIsTrue(Collection<String> jobType);

    /**
     * 根据参数查询是否有任务
     *
     * @param id 任务id
     * @return 定时任务
     */
    QuartzJob findQuartzJobByParamsEquals(Long id);

}
