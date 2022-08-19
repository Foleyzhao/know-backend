package com.cumulus.modules.quartz.utils;

import com.cumulus.exception.BadRequestException;
import com.cumulus.modules.quartz.entity.QuartzJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.sql.Timestamp;

import static org.quartz.TriggerBuilder.newTrigger;

/**
 * 定时任务管理中心
 *
 * @author zhaoff
 */
@Slf4j
@Component
public class QuartzManage {

    /**
     * 定时任务前缀
     */
    private static final String JOB_NAME = "TASK_";

    /**
     * 定时任务调度器
     */
    @Resource
    private Scheduler scheduler;

    /**
     * 新增定时任务
     *
     * @param quartzJob 定时任务
     */
    public void addJob(QuartzJob quartzJob) {
        try {
            // 构建job信息
            JobDetail jobDetail = JobBuilder.newJob(ExecutionJob.class)
                    .withIdentity(JOB_NAME + quartzJob.getId()).build();
            if (null != quartzJob.getPeriod()) {
                // 时间间隔
                // 创建 Trigger
                SimpleTrigger trigger = newTrigger()
                        .withIdentity(JOB_NAME + quartzJob.getId())
                        .startAt(quartzJob.getStartTime())
                        .withSchedule(SimpleScheduleBuilder
                                .simpleSchedule()
                                .withIntervalInSeconds(quartzJob.getPeriod().intValue())
                                .repeatForever())
                        .build();
                trigger.getJobDataMap().put(QuartzJob.JOB_KEY, quartzJob);
                // 执行定时任务
                scheduler.scheduleJob(jobDetail, trigger);
            } else {
                // cron 表达式
                // 创建 Trigger
                TriggerBuilder<CronTrigger> triggerBuilder = newTrigger()
                        .withIdentity(JOB_NAME + quartzJob.getId())
                        .withSchedule(CronScheduleBuilder.cronSchedule(quartzJob.getCronExpression()));
                if (quartzJob.getStartTime().after(new Timestamp(System.currentTimeMillis()))) {
                    triggerBuilder.startAt(quartzJob.getStartTime());
                }
                CronTrigger trigger = triggerBuilder.build();
                trigger.getJobDataMap().put(QuartzJob.JOB_KEY, quartzJob);
                // 执行定时任务
                scheduler.scheduleJob(jobDetail, trigger);
            }
            // 暂停任务
            if (quartzJob.getIsPause()) {
                pauseJob(quartzJob);
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Failed to create scheduled task", e);
            }
            throw new BadRequestException("创建定时任务失败");
        }
    }

    /**
     * 更新定时任务cron表达式
     *
     * @param quartzJob 定时任务
     */
    public void updateJobCron(QuartzJob quartzJob) {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(JOB_NAME + quartzJob.getId());
            Trigger trigger = scheduler.getTrigger(triggerKey);
            // 触发器不存在则创建一个触发器
            if (null == trigger) {
                addJob(quartzJob);
                trigger = scheduler.getTrigger(triggerKey);
            }
            if (null != quartzJob.getPeriod()) {
                // 时间间隔
                // 创建 Trigger
                trigger = newTrigger()
                        .withIdentity(JOB_NAME + quartzJob.getId())
                        .startAt(quartzJob.getStartTime())
                        .withSchedule(SimpleScheduleBuilder
                                .simpleSchedule()
                                .withIntervalInSeconds(quartzJob.getPeriod().intValue())
                                .repeatForever())
                        .build();
                trigger.getJobDataMap().put(QuartzJob.JOB_KEY, quartzJob);
                // 恢复定时任务
                scheduler.rescheduleJob(triggerKey, trigger);
            } else {
                // cron 表达式
                // 创建 Trigger
                trigger = newTrigger()
                        .withIdentity(JOB_NAME + quartzJob.getId())
                        .startAt(quartzJob.getStartTime())
                        .withSchedule(CronScheduleBuilder.cronSchedule(quartzJob.getCronExpression()))
                        .build();
                trigger.getJobDataMap().put(QuartzJob.JOB_KEY, quartzJob);
                // 恢复定时任务
                scheduler.rescheduleJob(triggerKey, trigger);
            }
            // 暂停任务
            if (quartzJob.getIsPause()) {
                pauseJob(quartzJob);
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Failed to update scheduled task", e);
            }
            throw new BadRequestException("更新定时任务失败");
        }
    }

    /**
     * 删除一个定时任务
     *
     * @param quartzJob 定时任务
     */
    public void deleteJob(QuartzJob quartzJob) {
        try {
            JobKey jobKey = JobKey.jobKey(JOB_NAME + quartzJob.getId());
            scheduler.pauseJob(jobKey);
            scheduler.deleteJob(jobKey);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Failed to delete scheduled task", e);
            }
            throw new BadRequestException("删除定时任务失败");
        }
    }

    /**
     * 恢复一个定时任务
     *
     * @param quartzJob 定时任务
     */
    public void resumeJob(QuartzJob quartzJob) {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(JOB_NAME + quartzJob.getId());
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            // 如果不存在则创建一个定时任务
            if (null == trigger) {
                addJob(quartzJob);
            }
            JobKey jobKey = JobKey.jobKey(JOB_NAME + quartzJob.getId());
            scheduler.resumeJob(jobKey);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Failed to restore scheduled task", e);
            }
            throw new BadRequestException("恢复定时任务失败");
        }
    }

    /**
     * 立即执行定时任务
     *
     * @param quartzJob 定时任务
     */
    public void runJobNow(QuartzJob quartzJob) {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(JOB_NAME + quartzJob.getId());
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            // 如果不存在则创建一个定时任务
            if (null == trigger) {
                addJob(quartzJob);
            }
            JobDataMap dataMap = new JobDataMap();
            dataMap.put(QuartzJob.JOB_KEY, quartzJob);
            JobKey jobKey = JobKey.jobKey(JOB_NAME + quartzJob.getId());
            scheduler.triggerJob(jobKey, dataMap);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Scheduled task execution failed", e);
            }
            throw new BadRequestException("定时任务执行失败");
        }
    }

    /**
     * 暂停定时任务
     *
     * @param quartzJob 定时任务
     */
    public void pauseJob(QuartzJob quartzJob) {
        try {
            JobKey jobKey = JobKey.jobKey(JOB_NAME + quartzJob.getId());
            scheduler.pauseJob(jobKey);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Failed to suspend the scheduled task", e);
            }
            throw new BadRequestException("定时任务暂停失败");
        }
    }

}
