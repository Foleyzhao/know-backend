package com.cumulus.modules.quartz.service;

import com.cumulus.modules.quartz.entity.QuartzJob;

import java.util.Date;
import java.util.Set;

/**
 * 定时任务服务接口
 *
 * @author zhaoff
 */
public interface QuartzJobService {

    /**
     * 创建定时任务
     *
     * @param resources 定时任务
     */
    void create(QuartzJob resources);

    /**
     * 编辑定时任务
     *
     * @param resources 定时任务
     */
    void update(QuartzJob resources);

    /**
     * 删除定时任务
     *
     * @param ids 定时任务ID集合
     */
    void delete(Set<Long> ids);

    /**
     * 根据ID查询定时任务
     *
     * @param id 定时任务ID
     * @return 定时任务
     */
    QuartzJob findById(Long id);

    /**
     * 变换定时任务状态（暂停/继续）
     *
     * @param quartzJob 定时任务
     */
    void updateIsPause(QuartzJob quartzJob);

    /**
     * 立即执行定时任务
     *
     * @param quartzJob 定时任务
     */
    void execution(QuartzJob quartzJob);

    /**
     * 根据参数ID查询定时任务
     *
     * @param id 参数ID
     * @return 定时任务
     */
    QuartzJob findByParamId(Long id);

    /**
     * 获取下次启动时间
     *
     * @param id jobId
     * @return 返回下次启动时间
     */
    Date getNextFireTime(Long id);
}
