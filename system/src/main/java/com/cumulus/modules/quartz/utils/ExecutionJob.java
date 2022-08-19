package com.cumulus.modules.quartz.utils;

import com.cumulus.config.thread.ThreadPoolExecutorUtil;
import com.cumulus.modules.quartz.config.JobRunner;
import com.cumulus.modules.quartz.entity.QuartzJob;
import com.cumulus.modules.quartz.entity.QuartzLog;
import com.cumulus.modules.quartz.repository.QuartzLogRepository;
import com.cumulus.modules.quartz.service.QuartzJobService;
import com.cumulus.utils.RedisUtils;
import com.cumulus.utils.SpringContextHolder;
import com.cumulus.utils.ThrowableUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Quartz Job
 *
 * @author zhaoff
 */
@Slf4j
@Async
public class ExecutionJob extends QuartzJobBean {

    /**
     * 执行线程池
     */
    private final static ThreadPoolExecutor EXECUTOR = ThreadPoolExecutorUtil.getCustomizePool();

    @Override
    public void executeInternal(JobExecutionContext context) {
        QuartzJob quartzJob = (QuartzJob) context.getMergedJobDataMap().get(QuartzJob.JOB_KEY);
        // 获取spring bean
        QuartzLogRepository quartzLogRepository = SpringContextHolder.getBean(QuartzLogRepository.class);
        QuartzJobService quartzJobService = SpringContextHolder.getBean(QuartzJobService.class);
        RedisUtils redisUtils = SpringContextHolder.getBean(RedisUtils.class);
        boolean defaultJob = JobRunner.DEFAULT_START_JOB_TYPE.contains(quartzJob.getJobType());
        QuartzLog quartzLog = new QuartzLog();
        quartzLog.setJobName(quartzJob.getJobName());
        quartzLog.setJobType(quartzJob.getJobType());
        quartzLog.setBeanName(quartzJob.getBeanName());
        quartzLog.setMethodName(quartzJob.getMethodName());
        quartzLog.setParams(quartzJob.getParams());
        quartzLog.setStartTime(quartzJob.getStartTime());
        quartzLog.setCronExpression(quartzJob.getCronExpression());
        quartzLog.setPeriod(quartzJob.getPeriod());
        // 记录当前时间，用于计算执行耗时
        long startTime = System.currentTimeMillis();
        try {
            // 执行任务
            if (log.isInfoEnabled() && !defaultJob) {
                log.info("Starts task, task name: " + quartzJob.getJobName());
            }
            QuartzRunnable task = new QuartzRunnable(quartzJob.getBeanName(), quartzJob.getMethodName(),
                    quartzJob.getParams());
            Future<?> future = EXECUTOR.submit(task);
            future.get();
            long times = System.currentTimeMillis() - startTime;
            quartzLog.setTime(times);
            // 任务状态
            quartzLog.setIsSuccess(true);
            if (log.isInfoEnabled() && !defaultJob) {
                log.info("End task, task name: " + quartzJob.getJobName() + ", execution time: " + times + " ms");
            }
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info("Task execution failed, task name: " + quartzJob.getJobName());
            }
            long times = System.currentTimeMillis() - startTime;
            quartzLog.setTime(times);
            // 任务状态
            quartzLog.setIsSuccess(false);
            quartzLog.setExceptionDetail(ThrowableUtils.getStackTrace(e).substring(0, 250));
            // 任务如果失败了则暂停
            quartzJob.setIsPause(false);
            // 更新状态
            quartzJobService.updateIsPause(quartzJob);
        } finally {
            if (!defaultJob) {
                quartzLogRepository.save(quartzLog);
            }
        }
    }
}
